package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for {@code POST /api/auth/register} to register the cinema manager account.
 *
 * <p>Password length constraints ({@code 8..100} characters) match security minimums and the
 * column length before BCrypt hashing.
 */
public class RegisterRequest {

    @NotBlank(message = "Cinema name is required")
    @Size(max = 100, message = "Cinema name cannot exceed 100 characters")
    private String name;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters")
    private String password;

    public RegisterRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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