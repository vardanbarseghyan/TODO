package com.vardan.todo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response returned by all endpoints when something goes wrong.
 *
 * Every error from your API will look like this in JSON:
 * {
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "Email already taken",
 *   "path": "/api/v1/auth/register",
 *   "timestamp": "2026-03-07T14:30:00",
 *   "fieldErrors": null  <-- only included when present (thanks to @JsonInclude)
 * }
 *
 * This makes it very easy for your frontend to parse and display errors consistently.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in JSON output
public class ErrorResponse {
    private int status;            // HTTP status code (e.g., 400, 404, 409)
    private String error;          // Short error name (e.g., "Conflict", "Not Found")
    private String message;        // Human-readable message (e.g., "Email already taken")
    private String path;           // The endpoint that was called (e.g., "/api/v1/auth/register")
    private LocalDateTime timestamp;

    // Optional: for validation errors, we can list which fields failed
    // Example: { "email": "must not be blank", "password": "must be at least 8 characters" }
    private Map<String, String> fieldErrors;
}
