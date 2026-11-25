package com.cinemaapp.cinema.exception;

/**
 * Custom unchecked exception thrown by services when a requested domain resource
 * (such as a {@code Movie}, {@code MovieImage}, or {@code Showtime}) does not exist.
 *
 * <p>Caught by {@link GlobalExceptionHandler#handleResourceNotFound} to return an HTTP 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new not-found exception with a descriptive message for the API error payload.
     *
     * @param message explanation of which resource and ID could not be found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}