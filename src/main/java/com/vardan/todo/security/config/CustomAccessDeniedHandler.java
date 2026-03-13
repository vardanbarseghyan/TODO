package com.vardan.todo.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vardan.todo.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Custom Access Denied Handler
 * =============================
 *
 * WHEN DOES THIS RUN?
 * --------------------
 * Spring Security calls this when an AUTHENTICATED user tries to access
 * a resource they don't have PERMISSION for. In other words:
 *   - The user HAS a valid token (they are logged in)
 *   - But their ROLE doesn't allow them to access this endpoint
 *
 * EXAMPLE:
 * --------
 * Imagine you later add an admin endpoint:
 *   .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
 *
 * If a user with Role.USER (not ADMIN) tries to call /api/v1/admin/something,
 * they ARE authenticated (valid JWT), but they DON'T have the right role.
 * That's when this handler fires and returns 403 Forbidden.
 *
 * HOW IS THIS DIFFERENT FROM AuthenticationEntryPoint?
 * ---------------------------------------------------
 * AuthenticationEntryPoint → "WHO are you?" (no token) → 401
 * AccessDeniedHandler      → "I KNOW who you are, but you CAN'T do this" → 403
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())           // 403
                .error("Forbidden")
                .message("You do not have permission to access this resource")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());       // 403
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}