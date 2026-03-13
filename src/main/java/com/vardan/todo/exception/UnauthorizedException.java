package com.vardan.todo.exception;

/**
 * Thrown when a user tries to perform an action they're not authorized for,
 * or when authentication fails (bad credentials, etc.)
 *
 * The GlobalExceptionHandler catches this and returns 401 UNAUTHORIZED.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("You are not authorized to perform this action.");
    }
}