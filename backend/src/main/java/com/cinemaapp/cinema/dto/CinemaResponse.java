package com.cinemaapp.cinema.dto;

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