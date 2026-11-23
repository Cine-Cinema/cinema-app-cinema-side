package com.cinemaapp.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "showtime",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_showtime_date_time",
                        columnNames = {"show_date", "show_time"}
                )
        }
)
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    @Column(
            name = "price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "seats_per_row", nullable = false)
    private Integer seatsPerRow;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Showtime() {
    }

    public Showtime(
            Long movieId,
            LocalDate showDate,
            LocalTime showTime,
            BigDecimal price,
            Integer totalSeats,
            Integer seatsPerRow
    ) {
        this.movieId = movieId;
        this.showDate = showDate;
        this.showTime = showTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.seatsPerRow = seatsPerRow;
        this.active = true;
    }

    public Long getId() {
        return id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}