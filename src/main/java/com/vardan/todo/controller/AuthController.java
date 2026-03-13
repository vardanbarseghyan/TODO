package com.vardan.todo.controller;

import com.vardan.todo.dto.request.LoginRequest;
import com.vardan.todo.dto.request.RefreshTokenRequest;
import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.dto.response.AuthResponse;
import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import com.vardan.todo.exception.RateLimitExceededException;
import com.vardan.todo.security.jwt.JwtProperties;
import com.vardan.todo.security.jwt.JwtService;
import com.vardan.todo.security.service.LoginRateLimitService;
import com.vardan.todo.security.service.TokenBlacklistService;
import com.vardan.todo.service.AuthService;
import com.vardan.todo.service.RefreshTokenService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final LoginRateLimitService loginRateLimitService;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest)
    {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(authResponse);

    }
    @PostMapping("/login")//Protect /login endpoint from brute-force attacks
    public ResponseEntity<AuthResponse> login(HttpServletRequest request,
                                              @Valid @RequestBody LoginRequest loginRequest) {

        String ip = request.getRemoteAddr();

        Bucket bucket = loginRateLimitService.resolveBucket(ip);//This gets the bucket associated with that IP.

        if (!bucket.tryConsume(1)) {//use 1 token from bucket, ete bucket chi mnacel reject enq anum requesty
            throw new RateLimitExceededException("Too many login attempts. Try again later.");
        }

        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> newRefreshToken(@Valid @RequestBody RefreshTokenRequest newRefreshToken) {//@valid
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
