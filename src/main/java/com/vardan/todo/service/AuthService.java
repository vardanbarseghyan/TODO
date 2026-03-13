package com.vardan.todo.service;

import com.vardan.todo.dto.request.LoginRequest;
import com.vardan.todo.dto.request.RefreshTokenRequest;
import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.dto.response.AuthResponse;
import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import com.vardan.todo.enums.AuthProvider;
import com.vardan.todo.enums.Role;
import com.vardan.todo.exception.EmailAlreadyExistsException;
import com.vardan.todo.exception.ResourceNotFoundException;
import com.vardan.todo.exception.TokenExpiredException;
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
            throw new EmailAlreadyExistsException(request.getEmail());
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
        /*This is the most important line in the login. It triggers the entire Spring Security check we built.
        It calls your CustomUserDetailsService, finds the user, and compares the passwords.
        If it fails, the code stops right there and returns a 401 Unauthorized.*/
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(//esi extends a anum Authentication tipy, authenticate emthody yndunma Authentication tipi object.
                        request.getEmail(),
                        request.getPassword()
                )
        );//Step 1 — Find the user. The provider calls customUserDetailsService.loadUserByUsername(email).
        /*Step 2 — Compare passwords. If the user IS found, the provider takes the raw password from the request (request.getPassword(), which is the plain text the user typed) and the hashed password from the database (user.getPassword(), which is the BCrypt hash). It calls passwordEncoder.matches(rawPassword, hashedPassword). BCrypt is clever — it doesn't decrypt the hash. Instead, it takes the raw password, applies the same salt and hashing algorithm, and checks if the result matches the stored hash. If they don't match, it throws BadCredentialsException.*/
        /*Step 3 — Account checks. After the password matches, the provider checks the boolean methods you implemented in your User entity: isEnabled(), isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired(). Right now you hardcoded most of these to return true (with comments like "hetagayum kpoxvi"), so they always pass. But in the future, if you implement account locking (e.g., after 5 failed login attempts), this is where it would be enforced.
If all three steps pass, authenticate() returns successfully and your code continues to the next line. If ANY step fails, an exception is thrown and your GlobalExceptionHandler catches it, returning a clean 401 response.*/


        // 2. If we reach here, user is valid. Fetch them from DB.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));
        // 3. Generate new tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        /*the refresh token from registration is still sitting in the database and hasn't expired yet. So why not just look it up and return it?
         The answer is a security principle called token rotation. */
        refreshTokenService.deleteByUser(user);
        refreshTokenService.createRefreshTokenEntity(refreshToken, user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refresh(RefreshTokenRequest newRefreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(newRefreshToken.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token"));

        // 2. Check if the token has expired
        // Logic: Is the "expiryDate" in the past?
        if (refreshTokenEntity.getExpiryDate().isBefore(Instant.now())) {
            // Optional: delete it from DB if it's expired
            refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
            throw new TokenExpiredException("Refresh token has expired. Please log in again.");
        }
        User user = refreshTokenEntity.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenString = jwtService.generateRefreshToken(user);
        //delete old refresh token entity
        refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
        refreshTokenService.createRefreshTokenEntity(refreshTokenString, user);//sra mej save-nela anum:

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .build();
    }

    public void logout(RefreshTokenRequest refreshToken, String accessToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.findByToken(refreshToken.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token"));

        refreshTokenService.deleteByUser(refreshTokenEntity.getUser());
        long expiration = jwtProperties.getAccessTokenExpiration();

        tokenBlacklistService.blacklistToken(accessToken, expiration);
        //Now the access token becomes invalid immediately.
    }

}
