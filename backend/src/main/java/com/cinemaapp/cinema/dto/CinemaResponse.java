package com.cinemaapp.cinema.dto;

/**
 * Response DTO exposing safe cinema account details returned after registration,
 * profile retrieval, and profile updates.
 *
 * <p>Deliberately omits the {@code password} field from {@link com.cinemaapp.cinema.entity.Cinema}
 * so the stored BCrypt hash is never serialized to API clients.
 */
public class CinemaResponse {

    private Long id;
    private String name;
    private String address;
    private String email;

    public CinemaResponse() {
    }

    public CinemaResponse(Long id, String name, String address, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }
}