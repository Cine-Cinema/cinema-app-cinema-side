package com.cinemaapp.cinema.dto;

/**
 * Response DTO returned by {@code POST /api/auth/login} containing the signed JWT
 * and the HTTP Authorization scheme ({@code "Bearer"}).
 */
public class LoginResponse {

    private String accessToken;
    private String tokenType;

    public LoginResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}