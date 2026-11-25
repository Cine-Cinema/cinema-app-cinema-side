package com.cinemaapp.cinema.dto;

import java.time.LocalDateTime;

/**
 * Standardized JSON error payload returned for security errors (401/403) and handled
 * API exceptions (400/404/409).
 *
 * <p>Automatically timestamps each error when constructed so clients receive consistent
 * diagnostic fields across both {@code GlobalExceptionHandler} and {@code ApiErrorResponseWriter}.
 */
public class ApiErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ApiErrorResponse(
            int status,
            String error,
            String message,
            String path
    ) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}