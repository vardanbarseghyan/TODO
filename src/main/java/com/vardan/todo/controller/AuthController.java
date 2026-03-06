package com.vardan.todo.controller;

import com.vardan.todo.dto.request.LoginRequest;
import com.vardan.todo.dto.request.RefreshTokenRequest;
import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.dto.response.AuthResponse;
import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import com.vardan.todo.security.jwt.JwtProperties;
import com.vardan.todo.security.jwt.JwtService;
import com.vardan.todo.security.service.TokenBlacklistService;
import com.vardan.todo.service.AuthService;
import com.vardan.todo.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
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
        AuthResponse authResponse = authService.refresh(newRefreshToken);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @RequestBody RefreshTokenRequest newRefreshToken)
    {
        String authHeader = request.getHeader("Authorization");
        String accessToken = authHeader.substring(7);

        //we need delete refresh token from db, and also store access token in Redis blacklist
        authService.logout(newRefreshToken, accessToken);

        return ResponseEntity.ok("Refresh token has been deleted");
    }
}
