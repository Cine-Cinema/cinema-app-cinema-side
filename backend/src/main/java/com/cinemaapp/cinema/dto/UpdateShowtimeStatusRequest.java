package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for {@code PATCH /api/showtimes/{id}/status} to activate or deactivate
 * a showtime. Uses wrapper {@link Boolean} with {@link NotNull} so omitting {@code active}
 * fails validation instead of silently defaulting to {@code false}.
 */
public class UpdateShowtimeStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;

    public UpdateShowtimeStatusRequest() {
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}