package com.cinemaapp.cinema.exception;

import com.cinemaapp.cinema.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility Spring {@link Component} for writing {@link ApiErrorResponse} JSON directly
 * to a raw {@link HttpServletResponse}.
 *
 * <p>Why this exists separately from {@link GlobalExceptionHandler}: servlet filters and
 * Spring Security handlers (such as {@link com.cinemaapp.cinema.config.JwtAuthenticationFilter}
 * and {@link com.cinemaapp.cinema.config.SecurityConfig}) execute before Spring MVC reaches
 * any controller, so {@code @RestControllerAdvice} cannot intercept their errors.
 */
@Component
public class ApiErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ApiErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes a standardized error payload as UTF-8 JSON into the HTTP response.
     *
     * @param request  the current HTTP request (used to record the failing URI path)
     * @param response the HTTP response to populate
     * @param status   HTTP status code and reason phrase to return (e.g. 401 or 403)
     * @param message  human-readable explanation of the error
     * @throws IOException if writing JSON to the response output stream fails
     */
    public void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        ApiErrorResponse error = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}