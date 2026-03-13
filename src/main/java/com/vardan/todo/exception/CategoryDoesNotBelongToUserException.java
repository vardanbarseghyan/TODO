package com.vardan.todo.exception;

public class CategoryDoesNotBelongToUserException extends RuntimeException {
    public CategoryDoesNotBelongToUserException() {
        super("Category doesn't belong to user");
    }
    public CategoryDoesNotBelongToUserException(String message) {
        super(message);
    }
}
