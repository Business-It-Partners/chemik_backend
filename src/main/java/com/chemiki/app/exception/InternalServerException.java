// folder: com.chemiki.app.exception
// purpose: Custom exception for internal server errors
package com.chemiki.app.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}