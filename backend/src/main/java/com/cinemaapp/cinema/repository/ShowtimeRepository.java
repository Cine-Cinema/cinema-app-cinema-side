package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Showtime} queries and reservation-aware checks.
 *
 * <p>Combines Spring Data derived query methods (for listing and filtering showtimes)
 * with native PostgreSQL queries against the {@code reservation} and {@code reservation_seat}
 * tables, which do not have JPA entity classes in this service.
 */
public interface ShowtimeRepository
        extends JpaRepository<Showtime, Long> {

    /**
     * Returns all showtimes on a given date, ordered chronologically by start time.
     *
     * @param showDate screening date to filter by
     * @return matching showtimes sorted ascending by {@code show_time}
     */
    List<Showtime> findByShowDateOrderByShowTimeAsc(
            LocalDate showDate
    );

    /**
     * Returns all showtimes for a specific movie, ordered chronologically by date and time.
     *
     * @param movieId target movie ID
     * @return matching showtimes sorted ascending by {@code show_date} then {@code show_time}
     */
    List<Showtime> findByMovieIdOrderByShowDateAscShowTimeAsc(
            Long movieId
    );

    /**
     * Returns all showtimes matching both a specific movie and date, ordered by start time.
     *
     * @param movieId  target movie ID
     * @param showDate target screening date
     * @return matching showtimes sorted ascending by {@code show_time}
     */
    List<Showtime> findByMovieIdAndShowDateOrderByShowTimeAsc(
            Long movieId,
            LocalDate showDate
    );

    /**
     * Counts committed reservations ({@code CONFIRMED} or {@code PAID}) for a showtime.
     *
     * <p>Uses a native SQL query against the {@code reservation} table (ignoring {@code REJECTED}
     * reservations) so {@code ShowtimeService} can block changes to movie, schedule, price,
     * or row layout once customers hold active tickets.
     *
     * @param showtimeId ID of the showtime being inspected
     * @return number of {@code CONFIRMED} or {@code PAID} reservations
     */
    @Query(
            value = """
                SELECT COUNT(*)
                FROM reservation
                WHERE showtime_id = :showtimeId
                  AND status IN ('CONFIRMED', 'PAID')
                """,
            nativeQuery = true
    )
    long countCommittedReservations(
            @Param("showtimeId") Long showtimeId
    );

    /**
     * Finds the largest seat number currently held by a {@code CONFIRMED} or {@code PAID}
     * reservation for the given showtime.
     *
     * <p>Joins {@code reservation_seat} with {@code reservation} so rejected bookings are
     * excluded, and uses {@code COALESCE(..., 0)} to return {@code 0} when no seats are
     * reserved yet. Used to prevent reducing {@code totalSeats} below an already booked seat.
     *
     * @param showtimeId ID of the showtime being updated
     * @return highest committed seat number, or {@code 0} if none are reserved
     */
    @Query(
            value = """
                SELECT COALESCE(MAX(rs.seat_number), 0)
                FROM reservation_seat rs
                JOIN reservation r
                    ON r.id = rs.reservation_id
                WHERE rs.showtime_id = :showtimeId
                  AND r.status IN ('CONFIRMED', 'PAID')
                """,
            nativeQuery = true
    )
    int findHighestReservedSeat(
            @Param("showtimeId") Long showtimeId
    );

    /**
     * Lists all seat numbers currently occupied by {@code CONFIRMED} or {@code PAID}
     * reservations for the given showtime, sorted in ascending seat order.
     *
     * <p>Joins {@code reservation_seat} and {@code reservation} on {@code reservation_id}
     * while matching {@code showtime_id} on both tables and filtering out {@code REJECTED}
     * reservations, returning an ordered list of occupied seat numbers for seat-map display.
     *
     * @param showtimeId ID of the showtime whose seat availability is requested
     * @return ascending list of reserved seat numbers
     */
    @Query(
            value = """
                SELECT rs.seat_number
                FROM reservation_seat rs
                JOIN reservation r
                    ON r.id = rs.reservation_id
                WHERE rs.showtime_id = :showtimeId
                  AND r.showtime_id = :showtimeId
                  AND r.status IN ('CONFIRMED', 'PAID')
                ORDER BY rs.seat_number ASC
                """,
            nativeQuery = true
    )
    List<Integer> findReservedSeatNumbers(
            @Param("showtimeId") Long showtimeId
    );
}