package com.vardan.todo.exception;

/**
 * Thrown when a user exceeds the allowed number of requests (rate limiting).
 *
 * This replaces the RuntimeException in your AuthController's login method:
 *   throw new RuntimeException("Too many login attempts. Try again later.")
 *
 * The GlobalExceptionHandler catches this and returns 429 TOO MANY REQUESTS,
 * which is the correct HTTP status for rate limiting.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException() {
        super("Too many requests. Please try again later.");
    }
}