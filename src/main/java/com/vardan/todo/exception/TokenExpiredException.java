package com.vardan.todo.exception;

/**
 * Thrown when a refresh token has expired.
 *
 * This replaces:
 *   throw new RuntimeException("Refresh token was expired. Please make a new signin request")
 *
 * The GlobalExceptionHandler catches this and returns 401 UNAUTHORIZED,
 * which tells the frontend: "You need to log in again."
 */
public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException() {
        super("Token has expired. Please log in again.");
    }
}