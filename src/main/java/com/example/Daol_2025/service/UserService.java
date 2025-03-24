package com.example.Daol_2025.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import com.example.Daol_2025.domain.User;
import com.example.Daol_2025.dto.AuthResponse;
import com.example.Daol_2025.dto.LoginRequest;
import com.example.Daol_2025.security.JwtTokenProvider;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.security.web.webauthn.api.AuthenticatorResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.google.common.util.concurrent.FutureCallback;


@Service
public class UserService {

    private final DatabaseReference usersRef;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final DatabaseReference userIdIndexRef = FirebaseDatabase.getInstance().getReference("userIdIndex");

    @Autowired
    private FirebaseDatabase firebaseDatabase;

    @Autowired
    public UserService(FirebaseDatabase firebaseDatabase) {
        this.usersRef = firebaseDatabase.getReference("users");
    }

//    @Value("${jwt.secret}")
//    private String SECRET_KEY;
//
//    @Value("${jwt.expiration}")
//    private long ACCESS_EXPIRATION;
//
//    @Value("${jwt.refreshExpiration}")
//    private long REFRESH_EXPIRATION;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    // 회원가입
    // Firebase Database paths must not contain '.', '#', '$', '[', or ']' 문자 금지
    public AuthResponse registerUser(User user) throws ExecutionException, InterruptedException {
        // 1. userId 유효성 검사 ('.', '#', '$', '[', ']' 사용 불가)
        if (user.getUserId().matches(".*[.#$\\[\\]].*")) {
            throw new IllegalArgumentException("아이디나 비밀번호에서 '.', '#', '$', '[', ']' 를 제외하고 입력해주세요");
        }

        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();


        // 2. userIdIndex에서 중복 체크 (userId가 이미 사용 중인지 확인)
        userIdIndexRef.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    future.completeExceptionally(new IllegalArgumentException("이미 존재하는 사용자입니다."));
                } else {
                    future.complete(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Database error: " + error.getMessage()));
            }
        });

        future.get();

        // 3. Firebase에서 자동 생성된 키 사용
        String firebaseKey = usersRef.push().getKey();

        // 4. 비밀번호 해싱
        user.setPassword(encoder.encode(user.getPassword()));

        // 5. 주소 정보 설정 (위치 정보 가져오기)
        if (user.getAddress() == null) {
            UserRegionService userRegionService = new UserRegionService();
            String location = userRegionService.getAddressFromPython();

            if (location.contains("error")) {
                throw new RuntimeException("주소 정보를 가져오는 데 실패했습니다");
            } else {
                user.setAddress(location);
            }
        }

        // 6. users 테이블에 저장 (Firebase 키 기반 저장)
        usersRef.child(firebaseKey).setValueAsync(user);

        // 7. userIdIndex 테이블에 (userId → Firebase 키) 저장
        userIdIndexRef.child(user.getUserId()).setValueAsync(firebaseKey);

        // 8. JWT 토큰 발급 후 반환
        return jwtTokenProvider.generateTokens(user.getUserId());
    }


    // 로그인
    public AuthResponse login(String userId, String password) throws ExecutionException, InterruptedException {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();

        // 1. userIdIndex에서 Firebase 키 찾기 (O(1) 조회)
        userIdIndexRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    future.completeExceptionally(new IllegalArgumentException("존재하지 않는 사용자입니다."));
                    return;
                }

                // Firebase 키 가져오기
                String firebaseKey = dataSnapshot.getValue(String.class);

                // 2. users 테이블에서 해당 Firebase 키를 기반으로 유저 정보 조회
                usersRef.child(firebaseKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userSnapshot) {
                        if (!userSnapshot.exists()) {
                            future.completeExceptionally(new IllegalArgumentException("존재하지 않는 사용자입니다."));
                            return;
                        }
                        future.complete(userSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException("Database error: " + error.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Database error: " + error.getMessage()));
            }
        });

        DataSnapshot snapshot = future.get();
        User user = snapshot.getValue(User.class);

        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // JWT 토큰 발급 후 반환
        return jwtTokenProvider.generateTokens(userId);
    }


//    // JWT 생성 메서드 (Access Token & Refresh Token)
//    private AuthResponse generateTokens(String userId) {
//        String accessToken = JWT.create()
//                .withSubject("user-auth")
//                .withClaim("userId", userId)
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
//                .sign(Algorithm.HMAC256(SECRET_KEY));
//
//        String refreshToken = JWT.create()
//                .withSubject("user-auth-refresh")
//                .withClaim("userId", userId)
//                .withIssuedAt(new Date())
//                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
//                .sign(Algorithm.HMAC256(SECRET_KEY));
//
//        return new AuthResponse(accessToken, refreshToken);
//    }
//
//    // JWT 검증 메서드
//    public String authenticate(String token) {
//        try {
//            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
//                    .withSubject("user-auth")
//                    .build();
//            DecodedJWT decodedJWT = verifier.verify(token);
//            return decodedJWT.getClaim("userId").asString();
//        } catch (JWTVerificationException e) {
//            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
//        }
//    }

    // 사용자 가져오기
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });
        DataSnapshot snapshot = future.get();
        List<User> users = new ArrayList<>();
        for(DataSnapshot userSnapshot : snapshot.getChildren()) {
            User user = userSnapshot.getValue(User.class);
            users.add(user);
        }
        return users;

    }

    // 사용자 조회 by email
    public UserRecord getUserByEmail(String email, String password) throws ExecutionException, InterruptedException, FirebaseAuthException {
        // Firebase Authentication에서는 직접적으로 이메일과 비밀번호로 로그인하는 메서드를 제공하지 않습니다.
        // 대신, 클라이언트 측에서 Firebase SDK를 사용하여 로그인하고, ID 토큰을 서버로 전달하는 방식으로 구현해야 합니다.
        UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
        return userRecord;
    }

    // 사용자 조회 by id
    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();

        // userIdIndex에서 firebaseKey 가져오기
        userIdIndexRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot indexSnapshot) { // indexSnapshot 에서 id 가 userId 인 datasnapshor 가져오기
                if (!indexSnapshot.exists()) {
                    future.completeExceptionally(new RuntimeException("존재하지 않는 사용자입니다."));
                    return;
                }

                String firebaseKey = indexSnapshot.getValue(String.class);

                // 해당 firebaseKey로 users 테이블 조회
                usersRef.child(firebaseKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userSnapshot) {
                        future.complete(userSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });

        DataSnapshot snapshot = future.get();
        User user = snapshot.getValue(User.class);

        if (user != null) {
            return user;
        } else {
            throw new RuntimeException("Id 나 Password 가 유효하지 않습니다.");
        }
    }

    // 토큰으로 user 가져오기
    public User getUserByToken(String token) throws ExecutionException, InterruptedException, FirebaseAuthException {
        String uid = jwtTokenProvider.getUserIdFromToken(token);

        return getUserById(uid);
    }

    // 유저정보 수정
    public String updateUserProfile(String token, User newUser) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = getUserByToken(token);
        System.out.println("ORIGIN USER : " + user);

        CompletableFuture<Void> future = new CompletableFuture<>();

        // userIdIndex에서 Firebase 키 찾기 (O(1) 조회)
        userIdIndexRef.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    future.completeExceptionally(new RuntimeException("존재하지 않는 사용자입니다."));
                    return;
                }

                // Firebase 키 가져오기
                String firebaseKey = dataSnapshot.getValue(String.class);

                Map<String, Object> updates = new HashMap<>();
                updates.put("userId", newUser.getUserId());  // 🔥 userId 변경 가능
                updates.put("name", newUser.getName());
                updates.put("password", encoder.encode(newUser.getPassword()));
                updates.put("address", newUser.getAddress());

                // 2. users 테이블에서 Firebase 키 기반으로 업데이트 실행
                ApiFuture<Void> apiFuture = usersRef.child(firebaseKey).updateChildrenAsync(updates);
                ApiFutures.addCallback(apiFuture, new ApiFutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // 3. userId가 변경되었다면 userIdIndex도 업데이트
                        if (!user.getUserId().equals(newUser.getUserId())) {
                            userIdIndexRef.child(user.getUserId()).removeValueAsync();
                            userIdIndexRef.child(newUser.getUserId()).setValueAsync(firebaseKey);
                        }
                        future.complete(null);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        future.completeExceptionally(t);
                    }
                }, MoreExecutors.directExecutor());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(new RuntimeException(databaseError.getMessage()));
            }
        });

        future.get();
        return "사용자 정보 업데이트 완료";
    }



    // 스크랩

}




