package com.cinemaapp.cinema.dto;

public class MovieResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final int durationMinutes;

    private String posterUrl;

    public MovieResponse(
            Long id,
            String title,
            String description,
            int durationMinutes

    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}