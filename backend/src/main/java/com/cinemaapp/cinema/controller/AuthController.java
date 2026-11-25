package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.CinemaResponse;
import com.cinemaapp.cinema.dto.RegisterRequest;
import com.cinemaapp.cinema.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinemaapp.cinema.dto.LoginRequest;
import com.cinemaapp.cinema.dto.LoginResponse;

/**
 * REST controller exposing public authentication endpoints for the cinema manager.
 *
 * <p>Both endpoints under {@code /api/auth} are marked {@code permitAll()} in
 * {@link com.cinemaapp.cinema.config.SecurityConfig} so an unauthenticated manager
 * can register the cinema account and obtain a JWT access token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers the cinema manager account (only one cinema account is permitted).
     *
     * <p>{@link Valid} triggers Bean Validation on {@link RegisterRequest} before this
     * method executes, returning HTTP 400 with field errors if any constraint fails.
     *
     * @param request registration details from the JSON body
     * @return HTTP 201 Created with the saved cinema profile (excluding password)
     */
    @PostMapping("/register")
    public ResponseEntity<CinemaResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        CinemaResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Authenticates the cinema manager with email and password and returns a JWT Bearer token.
     *
     * @param request login credentials from the JSON body
     * @return HTTP 200 OK containing the signed access token and token type
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}