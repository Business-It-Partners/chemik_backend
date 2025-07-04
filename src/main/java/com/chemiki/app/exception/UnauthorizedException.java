package com.chemiki.app.exception;

// exception/UnauthorizedException.java
// Purpose: Custom exception for handling unauthorized access errors (e.g., invalid OTP or token).

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}


