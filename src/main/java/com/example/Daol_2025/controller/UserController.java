package com.example.Daol_2025.controller;

import com.example.Daol_2025.domain.User;
import com.example.Daol_2025.dto.AuthResponse;
import com.example.Daol_2025.dto.LoginRequest;
import com.example.Daol_2025.security.JwtTokenProvider;
import com.example.Daol_2025.service.UserService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/users/")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // 회원 가입
    @PostMapping("/signup")
    public AuthResponse saveUser(@RequestBody User user) throws ExecutionException, InterruptedException {
        return userService.registerUser(user);
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestParam String refreshToken) {
        return jwtTokenProvider.getAccessToken(refreshToken);
    }

    // 로그인
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) throws ExecutionException, InterruptedException, FirebaseAuthException {
        String userId = request.getUserId();
        String password = request.getPassword();
        return userService.login(userId, password);
    }

    // 사용자 가져오기
    @GetMapping("/all")
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        return userService.getAllUsers();
    }

    @GetMapping("/profile")
    public User getUserbyToken(@RequestHeader("Authorization") String authorizationHeader) throws ExecutionException, InterruptedException, FirebaseAuthException {
        // "Bearer <token>"에서 "<token>" 부분만 추출
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 올바르지 않습니다.");
        }

        String token = authorizationHeader.substring(7); // "Bearer " 제거 후 토큰만 추출

        return userService.getUserByToken(token);
    }

    // 사용자 정보 업데이트
    @PostMapping("/profile/update")
    public String updateUser(@RequestHeader("Authorization") String authorizationHeader, @RequestBody User user) throws ExecutionException, InterruptedException, FirebaseAuthException {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더가 올바르지 않습니다.");
        }
        String token = authorizationHeader.substring(7); // "Bearer " 제거 후 토큰만 추출

        return userService.updateUserProfile(token, user);
    }
}
