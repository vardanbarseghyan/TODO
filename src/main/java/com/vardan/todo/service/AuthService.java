package com.vardan.todo.service;

import com.vardan.todo.dto.request.LoginRequest;
import com.vardan.todo.dto.request.RefreshTokenRequest;
import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.dto.response.AuthResponse;
import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import com.vardan.todo.enums.AuthProvider;
import com.vardan.todo.enums.Role;
import com.vardan.todo.repository.RefreshTokenRepository;
import com.vardan.todo.repository.UserRepository;
import com.vardan.todo.security.jwt.JwtProperties;
import com.vardan.todo.security.jwt.JwtService;
import com.vardan.todo.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    // --- REGISTER LOGIC ---
    public AuthResponse register(RegisterRequest request) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already taken");
        }

        // 2. Create user and HASH the password
        User user = userService.createUser(request);
        // 3. Generate tokens so they are logged in immediately
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.createRefreshTokenEntity(refreshToken, user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // --- LOGIN LOGIC ---
    public AuthResponse login(LoginRequest request) {
        // 1. The "Lie Detector" check
        // This will throw an exception if email/password is wrong
        /*This is the most important line in the login. It triggers the entire Spring Security check we built. It calls your CustomUserDetailsService, finds the user, and compares the passwords. If it fails, the code stops right there and returns a 401 Unauthorized.*/
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. If we reach here, user is valid. Fetch them from DB.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 3. Generate new tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.deleteByUser(user);
        refreshTokenService.createRefreshTokenEntity(refreshToken, user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refresh(RefreshTokenRequest newRefreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(newRefreshToken.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // 2. Check if the token has expired
        // Logic: Is the "expiryDate" in the past?
        if (refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
            // Optional: delete it from DB if it's expired
            refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        User user = refreshTokenEntity.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenString = jwtService.generateRefreshToken(user);
        //delete old refresh token entity
        refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
        //new refreshTokenEntity
//        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
//                .user(user)
//                .token(refreshTokenString)
//                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
//                .build();
        refreshTokenService.createRefreshTokenEntity(refreshTokenString, user);//sra mej save-nela anum:

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .build();
    }

    public void logout(RefreshTokenRequest request, String accessToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
        long expiration = jwtProperties.getAccessTokenExpiration();

        tokenBlacklistService.blacklistToken(accessToken, expiration);
        //Now the access token becomes invalid immediately.
    }

}
