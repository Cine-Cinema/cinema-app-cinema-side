package com.cinemaapp.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for a reservation_seat record.
 *
 * A seat is identified within a reservation by:
 * 1. The reservation ID
 * 2. The seat number
 *
 * Both values together form the primary key.
 */
@Embeddable
public class ReservationSeatId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "reservation_id", length = 36)
    private String reservationId;

    @Column(name = "seat_number")
    private Integer seatNumber;

    /**
     * Required by JPA.
     */
    public ReservationSeatId() {
    }

    public ReservationSeatId(
            String reservationId,
            Integer seatNumber
    ) {
        this.reservationId = reservationId;
        this.seatNumber = seatNumber;
    }

    public String getReservationId() {
        return reservationId;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    /**
     * Two composite IDs are equal when both their
     * reservation IDs and seat numbers match.
     */
    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ReservationSeatId that = (ReservationSeatId) object;

        return Objects.equals(reservationId, that.reservationId)
                && Objects.equals(seatNumber, that.seatNumber);
    }

    /**
     * Produces a hash code using both primary-key fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(reservationId, seatNumber);
    }
}