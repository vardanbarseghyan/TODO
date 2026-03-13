package com.vardan.todo.exception;

/**
 * Thrown when a user tries to register with an email that already exists in the database.
 *
 * WHY a custom exception instead of just RuntimeException?
 * --------------------------------------------------------
 * When you throw RuntimeException("Email already taken"), Spring doesn't know
 * WHAT went wrong — it just sees "something crashed" and returns 500.
 *
 * But when you throw EmailAlreadyExistsException, the GlobalExceptionHandler
 * can catch THIS SPECIFIC type and return a proper 409 CONFLICT response.
 *
 * Think of it like this:
 * - RuntimeException = "Something broke!" (unhelpful)
 * - EmailAlreadyExistsException = "A user tried to register with a taken email" (specific, actionable)
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        // super() passes the message up to RuntimeException
        // so getMessage() will return this formatted string
        super("Email already taken: " + email);
    }
}
