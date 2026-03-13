package com.vardan.todo.exception;

import com.vardan.todo.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GLOBAL EXCEPTION HANDLER
 * =========================
 *
 * HOW IT WORKS:
 * -------------
 * @RestControllerAdvice tells Spring:
 *   "Hey, if ANY controller throws an exception, don't crash — come here first."
 *
 * Each @ExceptionHandler method catches ONE specific type of exception
 * and returns a clean, consistent JSON response with the right HTTP status.
 *
 * BEFORE (without this class):
 *   POST /api/v1/auth/register → 500 Internal Server Error
 *   {
 *     "timestamp": "...",
 *     "status": 500,
 *     "error": "Internal Server Error",
 *     "trace": "java.lang.RuntimeException: Email already taken\n\tat com.vardan..."  ← UGLY + SECURITY RISK
 *   }
 *
 * AFTER (with this class):
 *   POST /api/v1/auth/register → 409 Conflict
 *   {
 *     "status": 409,
 *     "error": "Conflict",
 *     "message": "Email already taken: john@example.com",
 *     "path": "/api/v1/auth/register",
 *     "timestamp": "2026-03-07T14:30:00"
 *   }
 *
 * FLOW:
 *   Controller → Service throws exception → Spring catches it →
 *   GlobalExceptionHandler finds matching @ExceptionHandler → Returns clean ErrorResponse
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CategoryDoesNotBelongToUserException.class)
    public ResponseEntity<ErrorResponse> handleCategoryDoesNotBelongUser(CategoryDoesNotBelongToUserException ex ,HttpServletRequest request)
    {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())           // 409
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    // =====================================================
    // 1. EMAIL ALREADY EXISTS → 409 CONFLICT
    // =====================================================
    // Triggered when: authService.register() detects a duplicate email
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())           // 409
                .error("Conflict")
                .message(ex.getMessage())                       // "Email already taken: john@example.com"
                .path(request.getRequestURI())                  // "/api/v1/auth/register"
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // =====================================================
    // 2. RESOURCE NOT FOUND → 404 NOT FOUND
    // =====================================================
    // Triggered when: any .orElseThrow() in your services can't find an entity
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())          // 404
                .error("Not Found")
                .message(ex.getMessage())                       // "User not found with email: john@example.com"
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // =====================================================
    // 3. TOKEN EXPIRED → 401 UNAUTHORIZED
    // =====================================================
    // Triggered when: refresh token's expiryDate is in the past
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())       // 401
                .error("Unauthorized")
                .message(ex.getMessage())                       // "Token has expired. Please log in again."
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // =====================================================
    // 4. UNAUTHORIZED → 401 UNAUTHORIZED
    // =====================================================
    // Triggered when: user tries to access something they shouldn't
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())       // 401
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // =====================================================
    // 5. BAD CREDENTIALS → 401 UNAUTHORIZED
    // =====================================================
    // Triggered when: Spring Security's authenticationManager.authenticate()
    // fails because the email/password combination is wrong.
    // This is NOT your custom exception — it comes from Spring Security itself.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())       // 401
                .error("Unauthorized")
                .message("Invalid email or password")           // Don't reveal which one is wrong (security best practice)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // =====================================================
    // 6. RATE LIMIT EXCEEDED → 429 TOO MANY REQUESTS
    // =====================================================
    // Triggered when: Bucket4j rejects a login attempt
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())  // 429
                .error("Too Many Requests")
                .message(ex.getMessage())                       // "Too many login attempts. Try again later."
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    // =====================================================
    // 7. VALIDATION ERRORS → 400 BAD REQUEST
    // =====================================================
    // Triggered when: @Valid annotation on a @RequestBody fails.
    // This will be useful when you add validation annotations like
    // @NotBlank, @Email, @Size to your DTOs (RegisterRequest, LoginRequest, etc.)
    //
    // Example: If email is blank and password is too short, returns:
    // {
    //   "fieldErrors": {
    //     "email": "must not be blank",
    //     "password": "must be at least 8 characters"
    //   }
    // }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        // Loop through each field that failed validation
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(
                        fieldError.getField(),           // e.g., "email"
                        fieldError.getDefaultMessage()   // e.g., "must not be blank"
                )
        );

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())        // 400
                .error("Bad Request")
                .message("Validation failed")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)                       // Map of field → error message
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // =====================================================
    // 8. CATCH-ALL → 500 INTERNAL SERVER ERROR
    // =====================================================
    // This is the safety net. If ANY exception is thrown that doesn't match
    // the specific handlers above, this one catches it.
    //
    // IMPORTANT: We don't expose the real error message to the user.
    // Showing internal details (like stack traces) is a security risk.
    // Instead, we log it (you should add a logger) and return a generic message.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // TODO: Add proper logging here later (SLF4J)
        // log.error("Unexpected error on path {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        System.err.println("Unexpected error: " + ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
