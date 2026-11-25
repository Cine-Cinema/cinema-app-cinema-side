package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for {@code PUT /api/cinema/profile} used to update the cinema's display
 * name, optional address, and login email.
 */
public class UpdateCinemaProfileRequest {

    @NotBlank(message = "Cinema name is required")
    @Size(max = 100, message = "Cinema name cannot exceed 100 characters")
    private String name;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    public UpdateCinemaProfileRequest() {
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
}