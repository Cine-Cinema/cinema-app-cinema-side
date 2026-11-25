package com.cinemaapp.cinema.exception;

import com.cinemaapp.cinema.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers in the application.
 *
 * <p>{@link RestControllerAdvice} combines {@code @ControllerAdvice} and {@code @ResponseBody},
 * allowing methods annotated with {@link ExceptionHandler} to catch exceptions thrown from
 * controllers or services and return clean JSON responses instead of default stack traces.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation failures triggered by {@code @Valid} on controller {@code @RequestBody} DTOs.
     *
     * @param exception contains the field-level validation failures collected by Spring MVC
     * @return HTTP 400 Bad Request containing a map of {@code fieldName -> errorMessage}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        // LinkedHashMap preserves field insertion order for predictable JSON output
        Map<String, String> errors = new LinkedHashMap<>();

        // Keep the first error message per field if a field fails multiple constraints
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles missing domain records such as unknown movie IDs, posters, or showtimes.
     *
     * @param exception the not-found exception thrown by the service layer
     * @param request   current HTTP request used to populate the error path
     * @return HTTP 404 Not Found with a structured {@link ApiErrorResponse}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Translates PostgreSQL integrity errors (such as foreign-key or unique-constraint
     * violations) into an HTTP 409 Conflict response without leaking database internals.
     *
     * @param exception the Spring data-access wrapper around the SQL constraint error
     * @param request   current HTTP request used to populate the error path
     * @return HTTP 409 Conflict with a user-safe {@link ApiErrorResponse}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "This operation cannot be completed because the record is referenced by other records or violates a database constraint.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles malformed JSON payloads, missing request bodies, or unparseable field values
     * (for example an invalid date/time format) before controller validation runs.
     *
     * @param exception Jackson/Spring message-conversion error
     * @param request   current HTTP request used to populate the error path
     * @return HTTP 400 Bad Request with an {@link ApiErrorResponse}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request body is missing, contains invalid JSON, or has incompatible field values.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Formats {@link ResponseStatusException} instances thrown by services (such as 400, 401,
     * 404, or 409 business-rule failures) into the standard {@link ApiErrorResponse} structure.
     *
     * @param exception HTTP status exception raised in the service layer
     * @param request   current HTTP request used to populate the error path
     * @return response matching the exception's HTTP status code and reason
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(
                exception.getStatusCode().value()
        );

        ApiErrorResponse error = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                exception.getReason() != null
                        ? exception.getReason()
                        : "Request could not be completed.",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }
}