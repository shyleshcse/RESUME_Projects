package com.hospital.exception;

import org.springframework.http.HttpStatus;

/**
 * Our own error type. Whenever something goes wrong (wrong password, slot already booked ...)
 * we write:   throw new ApiException(HttpStatus.CONFLICT, "Slot already booked");
 * GlobalExceptionHandler then turns it into a neat JSON error for React.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
