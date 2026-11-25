package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for scheduling or updating a showtime ({@code POST /api/showtimes} and
 * {@code PUT /api/showtimes/{id}}).
 *
 * <p>Uses {@link DecimalMin} ({@code > 0.00}) and {@link Digits} ({@code integer = 8, fraction = 2})
 * on {@code price} to match PostgreSQL's {@code NUMERIC(10,2)} column definition.
 */
public class ShowtimeRequest {

    @NotNull(message = "Movie ID is required")
    @Positive(message = "Movie ID must be positive")
    private Long movieId;

    @NotNull(message = "Show date is required")
    private LocalDate showDate;

    @NotNull(message = "Show time is required")
    private LocalTime showTime;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(
            value = "0.00",
            inclusive = false,
            message = "Ticket price must be greater than zero"
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Ticket price must have at most 8 integer digits and 2 decimal places"
    )
    private BigDecimal price;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be greater than zero")
    private Integer totalSeats;

    @NotNull(message = "Seats per row is required")
    @Positive(message = "Seats per row must be greater than zero")
    private Integer seatsPerRow;

    public ShowtimeRequest() {
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public void setShowDate(LocalDate showDate) {
        this.showDate = showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public void setShowTime(LocalTime showTime) {
        this.showTime = showTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(Integer seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }
}