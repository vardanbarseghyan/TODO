package com.vardan.todo.controller;

import com.vardan.todo.dto.request.LoginRequest;
import com.vardan.todo.dto.request.RefreshTokenRequest;
import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.dto.response.AuthResponse;
import com.vardan.todo.exception.RateLimitExceededException;
import com.vardan.todo.service.AuthService;
import com.vardan.todo.security.service.LoginRateLimitService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Register, login, refresh tokens, and logout")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final LoginRateLimitService loginRateLimitService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns access and refresh tokens. "
                    + "The user is automatically logged in after registration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully — tokens returned"),
            @ApiResponse(responseCode = "400", description = "Validation error — check email format, password length, etc."),
            @ApiResponse(responseCode = "409", description = "Email already taken — this email is already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest)
    {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
            summary = "Login with email and password",
            description = "Authenticates the user and returns new access and refresh tokens. "
                    + "Rate limited to 5 attempts per minute per IP address to prevent brute-force attacks."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — tokens returned"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "429", description = "Too many login attempts — try again in 1 minute")
    })
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

    @Operation(
            summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new pair of access and refresh tokens. "
                    + "The old refresh token is deleted and a new one is created (token rotation). "
                    + "Use this when your access token expires to avoid forcing the user to log in again."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Refresh token has expired — user must log in again"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found — may have already been used or revoked")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> newRefreshToken(@Valid @RequestBody RefreshTokenRequest newRefreshToken) {//@valid
        AuthResponse authResponse = authService.refresh(newRefreshToken);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
            summary = "Logout",
            description = "Invalidates the current session by deleting the refresh token from the database "
                    + "and blacklisting the access token in Redis. Both tokens become unusable after this call. "
                    + "Requires a valid access token in the Authorization header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Not authenticated — provide a valid access token"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found")
    })
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
