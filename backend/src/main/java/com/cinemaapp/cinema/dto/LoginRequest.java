package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for {@code POST /api/auth/login} carrying cinema manager credentials.
 *
 * <p>Bean Validation annotations ({@link NotBlank}, {@link Email}) ensure malformed
 * requests fail fast with HTTP 400 before hitting the database or BCrypt verification.
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}