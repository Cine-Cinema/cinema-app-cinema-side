package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.ReservationSeat;
import com.cinemaapp.cinema.entity.ReservationSeatId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Provides database operations for seats assigned to reservations.
 *
 * The entity uses a composite primary key represented by
 * ReservationSeatId.
 */
public interface ReservationSeatRepository
        extends JpaRepository<ReservationSeat, ReservationSeatId> {

    /**
     * Retrieves seat records for a particular showtime.
     *
     * This returns records without filtering reservation status.
     * Status-aware availability calculations must use the
     * appropriate reservation-status filter.
     */
    List<ReservationSeat> findByShowtimeId(
            Long showtimeId
    );

    /**
     * Retrieves all seat records belonging to one reservation.
     *
     * The underscore explicitly indicates traversal into
     * the embedded ID's reservationId property.
     */
    List<ReservationSeat> findById_ReservationId(
            String reservationId
    );
}