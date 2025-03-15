package com.example.Daol_2025.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.Daol_2025.dto.AuthResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long ACCESS_EXPIRATION;

    @Value("${jwt.refreshExpiration}")
    private long REFRESH_EXPIRATION;

    private SecretKey key;

    public JwtTokenProvider() {}

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        //System.out.println(keyBytes);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        System.out.println(this.key);
    }

    public AuthResponse getAccessToken(String refreshToken) {
        try {
            // refreshToken 검증
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                    .withSubject("user-auth")
                    .build();
            DecodedJWT decodedJWT = verifier.verify(refreshToken);

            // 토큰에서 userId 추출
            String userId = decodedJWT.getClaim("userId").asString();

            // 새 accessToken 발급
            return generateTokens(userId);
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }


    // JWT 생성 메서드 (Access Token & Refresh Token)
    public AuthResponse generateTokens(String userId) {
        String accessToken = JWT.create()
                .withSubject("user-auth")
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .sign(Algorithm.HMAC256(secretKey));

        String refreshToken = JWT.create()
                .withSubject("user-auth-refresh")
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .sign(Algorithm.HMAC256(secretKey));

        return new AuthResponse(accessToken, refreshToken);
    }


    // JWT 검증 메서드
    public Boolean authenticate(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                    .withSubject("user-auth")
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // 토큰에서 사용자 정보 추출
    public Authentication getAuthentication(String token) {
        // System.out.println(">>>>>>>>>>>>>>>>> 키 : " + secretKey);
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).build()
                .parseClaimsJws(token).getBody();

        UserDetails userDetails = User.builder()
                .username(claims.getSubject())
                .password("") // 비밀번호는 필요 없음
                .roles("USER") // 기본 역할 부여
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    public String getUserIdFromToken(String token) {
        try {
            // `auth0.jwt` 라이브러리로 JWT 검증 및 디코딩
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                    .withSubject("user-auth")
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);

            // "userId" 클레임 값 추출
            return decodedJWT.getClaim("userId").asString();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.", e);
        }
    }
}
