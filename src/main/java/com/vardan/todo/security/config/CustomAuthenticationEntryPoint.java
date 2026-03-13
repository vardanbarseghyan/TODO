package com.vardan.todo.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vardan.todo.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom Authentication Entry Point
 * ==================================
 *
 * WHEN DOES THIS RUN?
 * --------------------
 * Spring Security calls this when an UNAUTHENTICATED request
 * tries to access a PROTECTED endpoint. In other words:
 *   - The request has NO token at all, OR
 *   - The token was invalid/expired and JwtAuthFilter couldn't authenticate the user
 *
 * WHAT DID IT DO BEFORE (without this class)?
 * -------------------------------------------
 * Spring Security returned a raw 403 Forbidden with an empty body.
 * This was confusing because:
 *   1. 403 means "you're not allowed" — but the real problem is "you didn't identify yourself"
 *   2. The empty body gave the frontend nothing useful to display
 *
 * WHAT DOES IT DO NOW (with this class)?
 * --------------------------------------
 * Returns a clean 401 Unauthorized with your standard ErrorResponse JSON:
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "You must provide a valid access token to access this resource",
 *   "path": "/api/v1/todos",
 *   "timestamp": "2026-03-12T14:30:00"
 * }
 *
 * WHY CAN'T WE USE @RestControllerAdvice HERE?
 * ---------------------------------------------
 * Great question! Your GlobalExceptionHandler uses @RestControllerAdvice,
 * which only catches exceptions thrown INSIDE controllers.
 * But this 401 rejection happens BEFORE the request reaches any controller —
 * it's blocked at the Spring Security filter level.
 * That's why we need to manually write the JSON response using ObjectMapper.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Build your standard ErrorResponse (same format as GlobalExceptionHandler uses)
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())        // 401
                .error("Unauthorized")
                .message("You must provide a valid access token to access this resource")
                .path(request.getRequestURI())                  // e.g., "/api/v1/todos"
                .timestamp(LocalDateTime.now())
                .build();

        // Set the HTTP response properties
        response.setStatus(HttpStatus.UNAUTHORIZED.value());    // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // "application/json"

        // Convert ErrorResponse object to JSON string and write it to the response body
        // We have to do this manually because we're outside the controller layer —
        // Spring's automatic JSON conversion (@RestController) doesn't work here.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // So it can serialize LocalDateTime properly
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}