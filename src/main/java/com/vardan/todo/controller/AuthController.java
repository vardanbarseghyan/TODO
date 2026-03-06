package com.vardan.todo.controller;

import com.vardan.todo.dto.request.LoginRequest;
import com.vardan.todo.dto.request.RefreshTokenRequest;
import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.dto.response.AuthResponse;
import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import com.vardan.todo.security.jwt.JwtProperties;
import com.vardan.todo.security.jwt.JwtService;
import com.vardan.todo.service.AuthService;
import com.vardan.todo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest)
    {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(authResponse);

    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest)
    {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> newRefreshToken(@RequestBody RefreshTokenRequest newRefreshToken) {
        AuthResponse authResponse = authService.newRefreshToken(newRefreshToken);
//        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(newRefreshToken.getRefreshToken())
//                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
//
//        // 2. Check if the token has expired
//        // Logic: Is the "expiryDate" in the past?
//        if (refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
//            // Optional: delete it from DB if it's expired
//            refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
//            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
//        }
//        User user = refreshTokenEntity.getUser();
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshTokenString = jwtService.generateRefreshToken(user);
//        //delete old refresh token entity
//        refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
//        //new refreshTokenEntity
//        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
//                .user(user)
//                .token(refreshTokenString)
//                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
//                .build();
//        refreshTokenService.saveRefreshToken(newRefreshTokenEntity);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest newRefreshToken)
    {
        authService.logout(newRefreshToken);
        return ResponseEntity.ok("Refresh token has been deleted");
    }
}
