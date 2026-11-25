package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Reservation;
import com.cinemaapp.cinema.enums.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Provides database operations for customer reservations.
 *
 * Spring Data JPA automatically implements standard CRUD methods
 * and generates queries from the method names below.
 */
public interface ReservationRepository
        extends JpaRepository<Reservation, String> {

    /**
     * Retrieves all reservations belonging to one showtime,
     * with the newest reservations first.
     */
    List<Reservation> findByShowtimeIdOrderByCreatedAtDesc(
            Long showtimeId
    );

    /**
     * Retrieves reservations for a particular showtime
     * having any of the specified statuses.
     */
    List<Reservation> findByShowtimeIdAndStatusIn(
            Long showtimeId,
            Collection<ReservationStatus> statuses
    );
}