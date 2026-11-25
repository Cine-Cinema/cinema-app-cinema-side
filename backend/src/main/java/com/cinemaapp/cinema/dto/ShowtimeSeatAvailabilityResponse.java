package com.cinemaapp.cinema.dto;

import java.util.List;

/**
 * Response DTO returned by {@code GET /api/showtimes/{id}/seats} summarizing hall capacity
 * and current seat occupancy for a showtime.
 */
public class ShowtimeSeatAvailabilityResponse {

    private final Long showtimeId;
    private final Integer totalSeats;
    private final Integer seatsPerRow;
    private final List<Integer> reservedSeats;
    private final Integer availableSeats;
    private final boolean active;

    public ShowtimeSeatAvailabilityResponse(
            Long showtimeId,
            Integer totalSeats,
            Integer seatsPerRow,
            List<Integer> reservedSeats,
            Integer availableSeats,
            boolean active
    ) {
        this.showtimeId = showtimeId;
        this.totalSeats = totalSeats;
        this.seatsPerRow = seatsPerRow;
        // Defensive immutable copy prevents callers from mutating the returned seat list
        this.reservedSeats = List.copyOf(reservedSeats);
        this.availableSeats = availableSeats;
        this.active = active;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public List<Integer> getReservedSeats() {
        return reservedSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public boolean isActive() {
        return active;
    }
}