package com.vardan.todo.exception;

/**
 * Thrown when a requested resource is not found in the database.
 *
 * This replaces patterns like:
 *   .orElseThrow(() -> new RuntimeException("User not found"))
 *   .orElseThrow(() -> new RuntimeException("Refresh token not found"))
 *
 * With a clean, reusable exception:
 *   .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()))
 *
 * The GlobalExceptionHandler catches this and returns 404 NOT FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Simple constructor with just a message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Detailed constructor that builds a descriptive message.
     *
     * Example usage:
     *   new ResourceNotFoundException("User", "email", "john@example.com")
     *   → message: "User not found with email: john@example.com"
     *
     *   new ResourceNotFoundException("Todo", "id", todoId)
     *   → message: "Todo not found with id: 550e8400-e29b-..."
     *
     * @param resourceName  The type of entity (e.g., "User", "Todo", "Category")
     * @param fieldName     The field used to search (e.g., "email", "id")
     * @param fieldValue    The actual value that wasn't found
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}