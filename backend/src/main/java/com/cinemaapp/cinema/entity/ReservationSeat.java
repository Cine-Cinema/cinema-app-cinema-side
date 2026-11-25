package com.cinemaapp.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represents one reserved seat belonging to one reservation.
 *
 * For example, a reservation containing seats 5 and 6
 * produces two ReservationSeat records.
 *
 * PostgreSQL additionally enforces uniqueness for the
 * combination of showtime_id and seat_number.
 */
@Entity
@Table(name = "reservation_seat")
public class ReservationSeat {

    /**
     * Composite primary key containing reservation ID
     * and seat number.
     */
    @EmbeddedId
    private ReservationSeatId id;

    /**
     * Identifies the showtime to which this seat belongs.
     */
    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    /**
     * Required by JPA.
     */
    public ReservationSeat() {
    }

    public ReservationSeat(
            String reservationId,
            Long showtimeId,
            Integer seatNumber
    ) {

        this.id = new ReservationSeatId(
                reservationId,
                seatNumber
        );

        this.showtimeId = showtimeId;
    }

    public ReservationSeatId getId() {
        return id;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public String getReservationId() {
        return id.getReservationId();
    }

    public Integer getSeatNumber() {
        return id.getSeatNumber();
    }
}