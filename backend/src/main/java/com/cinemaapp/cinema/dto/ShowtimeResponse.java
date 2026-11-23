package com.cinemaapp.cinema.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ShowtimeResponse {

    private final Long id;
    private final Long movieId;
    private final String movieTitle;

    private final LocalDate showDate;
    private final LocalTime showTime;

    private final LocalDate endDate;
    private final LocalTime endTime;

    private final BigDecimal price;

    private final Integer totalSeats;
    private final Integer seatsPerRow;

    private final boolean active;

    public ShowtimeResponse(
            Long id,
            Long movieId,
            String movieTitle,
            LocalDate showDate,
            LocalTime showTime,
            LocalDate endDate,
            LocalTime endTime,
            BigDecimal price,
            Integer totalSeats,
            Integer seatsPerRow,
            boolean active
    ) {
        this.id = id;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.showDate = showDate;
        this.showTime = showTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.seatsPerRow = seatsPerRow;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public LocalDate getShowDate() {
        return showDate;
    }

    public LocalTime getShowTime() {
        return showTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public boolean isActive() {
        return active;
    }
}