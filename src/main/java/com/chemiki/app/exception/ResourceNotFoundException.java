package com.chemiki.app.exception;

// exception/ResourceNotFoundException.java
// Purpose: Custom exception for handling resource not found errors (e.g., user not found).

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}