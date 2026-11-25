package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a movie ({@code POST /api/movies} and {@code PUT /api/movies/{id}}).
 *
 * <p>Validation limits ({@code 150} chars for title, {@code 500} chars for description,
 * positive {@code durationMinutes}) mirror the PostgreSQL column constraints in the {@code movie} table.
 */
public class MovieRequest {

    @NotBlank(message = "Movie title is required")
    @Size(max = 150, message = "Movie title cannot exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Movie duration is required")
    @Positive(message = "Movie duration must be greater than zero")
    private Integer durationMinutes;

    public MovieRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}