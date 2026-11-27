package com.cinemaapp.cinema.service;

import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import com.cinemaapp.cinema.dto.ShowtimeRequest;
import com.cinemaapp.cinema.dto.ShowtimeResponse;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.entity.Showtime;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.repository.MovieRepository;
import com.cinemaapp.cinema.repository.ShowtimeRepository;
import com.cinemaapp.cinema.dto.ShowtimeSeatAvailabilityResponse;
import com.cinemaapp.cinema.kafka.CatalogRefreshRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * Service managing showtime scheduling, updates, active status, and seat availability.
 *
 * <p>Because this cinema has a single hall, all scheduled screenings share one physical
 * screen and must not overlap in time. All "current time" comparisons use the cinema's
 * local timezone ({@code Asia/Colombo}) rather than the server's default clock.
 */
@Service
public class ShowtimeService {

    /** Cinema operating timezone used to validate that scheduled/reactivated showtimes are in the future. */
    private static final ZoneId CINEMA_TIME_ZONE =
            ZoneId.of("Asia/Colombo");

    private final ShowtimeRepository showtimeRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final MovieRepository movieRepository;

    public ShowtimeService(
            ShowtimeRepository showtimeRepository,
            MovieRepository movieRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Lists showtimes filtered optionally by screening date and/or movie ID, sorted chronologically.
     *
     * @param date    optional screening date filter
     * @param movieId optional positive movie ID filter
     * @return matching showtimes with computed end date/time
     * @throws ResponseStatusException with HTTP 400 if {@code movieId} is provided and non-positive
     */
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getAllShowtimes(
            LocalDate date,
            Long movieId
    ) {

        // Validate optional movie ID
        if (movieId != null && movieId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Movie ID must be positive"
            );
        }

        List<Showtime> showtimes;

        // Case 1: Filter by both date and movie ID
        if (date != null && movieId != null) {

            showtimes =
                    showtimeRepository
                            .findByMovieIdAndShowDateOrderByShowTimeAsc(
                                    movieId,
                                    date
                            );

        }

        // Case 2: Filter by date only
        else if (date != null) {

            showtimes =
                    showtimeRepository
                            .findByShowDateOrderByShowTimeAsc(date);

        }

        // Case 3: Filter by movie ID only
        else if (movieId != null) {

            showtimes =
                    showtimeRepository
                            .findByMovieIdOrderByShowDateAscShowTimeAsc(movieId);

        }

        // Case 4: Retrieve every showtime
        else {

            showtimes = showtimeRepository.findAll(
                    Sort.by("showDate", "showTime")
            );

        }

        // Convert Showtime entities into response DTOs
        return showtimes.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Schedules a new showtime after validating the movie, seating layout, future start time,
     * and absence of overlapping screenings in the single hall.
     *
     * @param request validated showtime creation payload
     * @return created {@link ShowtimeResponse} with computed end date/time
     * @throws ResourceNotFoundException if the referenced movie does not exist
     * @throws ResponseStatusException   with HTTP 400 for past times or invalid seating, or 409 for overlaps
     */
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {

        // Step 1: Find the selected movie
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + request.getMovieId()
                ));

        // Step 2: Validate seating configuration
        validateSeatingConfiguration(request);

        // Step 3: Calculate screening start time
        LocalDateTime newStart = LocalDateTime.of(
                request.getShowDate(),
                request.getShowTime()
        );

        // Step 4: Calculate screening end time (handles screenings that cross midnight into the next day)
        LocalDateTime newEnd = newStart.plusMinutes(
                movie.getDurationMinutes()
        );

        // Step 5: Reject screenings scheduled in the past or at the exact current minute
        if (!newStart.isAfter(
                LocalDateTime.now(CINEMA_TIME_ZONE)
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Showtime must be scheduled in the future"
            );
        }

        // Step 6: Check overlapping screenings against all existing showtimes
        validateNoOverlap(newStart, newEnd, null);

        // Step 7: Create the Showtime entity
        Showtime showtime = new Showtime(
                movie.getId(),
                request.getShowDate(),
                request.getShowTime(),
                request.getPrice(),
                request.getTotalSeats(),
                request.getSeatsPerRow()
        );

        // Step 8: Save and flush immediately so database constraint violations (e.g. uq_showtime_date_time)
        // occur inside this method rather than at transaction commit time
        Showtime savedShowtime =
                showtimeRepository.saveAndFlush(showtime);

        eventPublisher.publishEvent(
                new CatalogRefreshRequestedEvent(showtime.getId())
        );
        // Step 9: Return the response
        return toResponse(savedShowtime, movie);
    }

    /**
     * Ensures a single row is not configured with more seats than the total capacity of the hall.
     */
    private void validateSeatingConfiguration(
            ShowtimeRequest request
    ) {

        if (request.getSeatsPerRow() > request.getTotalSeats()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seats per row cannot exceed total seats"
            );
        }
    }

    /**
     * Checks that the proposed {@code [newStart, newEnd)} screening interval does not overlap
     * with any other screening in the single cinema hall.
     *
     * <p>Uses standard half-open interval intersection logic:
     * {@code newStart < existingEnd && newEnd > existingStart}. Back-to-back screenings where
     * {@code newStart == existingEnd} are allowed.
     *
     * @param newStart           proposed screening start timestamp
     * @param newEnd             proposed screening end timestamp
     * @param excludedShowtimeId ID of the showtime being updated (so it does not conflict with itself),
     *                           or {@code null} when creating a new showtime
     */
    private void validateNoOverlap(
            LocalDateTime newStart,
            LocalDateTime newEnd,
            Long excludedShowtimeId
    ) {

        List<Showtime> existingShowtimes =
                showtimeRepository.findAll();

        for (Showtime existing : existingShowtimes) {

            // Skip the showtime currently being updated
            if (excludedShowtimeId != null &&
                    excludedShowtimeId.equals(existing.getId())) {
                continue;
            }

            Movie existingMovie = movieRepository.findById(
                    existing.getMovieId()
            ).orElseThrow(() -> new IllegalStateException(
                    "Movie missing for showtime: " + existing.getId()
            ));

            LocalDateTime existingStart = LocalDateTime.of(
                    existing.getShowDate(),
                    existing.getShowTime()
            );

            LocalDateTime existingEnd = existingStart.plusMinutes(
                    existingMovie.getDurationMinutes()
            );

            // Half-open interval overlap check: back-to-back times (equal boundary) do not overlap
            boolean overlaps =
                    newStart.isBefore(existingEnd)
                            && newEnd.isAfter(existingStart);

            if (overlaps) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Showtime overlaps with existing showtime ID: "
                                + existing.getId()
                );
            }
        }
    }

    /**
     * Builds a {@link ShowtimeResponse} using the associated {@link Movie} to compute
     * {@code endDate} and {@code endTime} (which may fall on the next calendar day for late shows).
     */
    private ShowtimeResponse toResponse(
            Showtime showtime,
            Movie movie
    ) {

        LocalDateTime startDateTime = LocalDateTime.of(
                showtime.getShowDate(),
                showtime.getShowTime()
        );

        LocalDateTime endDateTime = startDateTime.plusMinutes(
                movie.getDurationMinutes()
        );

        return new ShowtimeResponse(
                showtime.getId(),
                movie.getId(),
                movie.getTitle(),
                showtime.getShowDate(),
                showtime.getShowTime(),
                endDateTime.toLocalDate(),
                endDateTime.toLocalTime(),
                showtime.getPrice(),
                showtime.getTotalSeats(),
                showtime.getSeatsPerRow(),
                showtime.isActive()
        );
    }

    private ShowtimeResponse toResponse(Showtime showtime) {

        Movie movie = movieRepository
                .findById(showtime.getMovieId())
                .orElseThrow(() -> new IllegalStateException(
                        "Movie missing for showtime ID: "
                                + showtime.getId()
                ));

        return toResponse(showtime, movie);
    }

    /**
     * Fetches a single showtime by its ID.
     *
     * @param id showtime primary key
     * @return matching {@link ShowtimeResponse}
     * @throws ResourceNotFoundException if no showtime exists with {@code id}
     */
    @Transactional(readOnly = true)
    public ShowtimeResponse getShowtimeById(Long id) {

        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with id: " + id
                ));

        return toResponse(showtime);
    }

    /**
     * Updates an existing showtime while protecting customers who already hold confirmed/paid tickets.
     *
     * <p>Once {@code CONFIRMED} or {@code PAID} reservations exist, movie, date, time, price, and
     * {@code seatsPerRow} are locked; only {@code totalSeats} can still be adjusted, and even then
     * not below the highest reserved seat number.
     *
     * @param id      ID of the showtime to update
     * @param request validated new showtime values
     * @return updated {@link ShowtimeResponse}
     * @throws ResourceNotFoundException if the showtime or movie does not exist
     * @throws ResponseStatusException   with HTTP 400 for invalid time/seating, or 409 for reservation
     *                                   conflicts or hall schedule overlaps
     */
    @Transactional
    public ShowtimeResponse updateShowtime(
            Long id,
            ShowtimeRequest request
    ) {

        // 1. Find the existing showtime
        Showtime showtime = showtimeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with id: " + id
                ));

        // 2. Find the selected movie
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + request.getMovieId()
                ));

        // 3. Validate seating configuration
        validateSeatingConfiguration(request);

        // 4. Calculate the updated screening interval
        LocalDateTime newStart = LocalDateTime.of(
                request.getShowDate(),
                request.getShowTime()
        );

        LocalDateTime newEnd = newStart.plusMinutes(
                movie.getDurationMinutes()
        );

        // 5. Reject an updated start time that is not in the future
        if (!newStart.isAfter(
                LocalDateTime.now(CINEMA_TIME_ZONE)
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Showtime must be scheduled in the future"
            );
        }

        // 6. Lock core screening details (movie, schedule, price, row layout) once tickets are committed
        long reservationCount =
                showtimeRepository.countCommittedReservations(id);

        if (reservationCount > 0) {

            // Use BigDecimal.compareTo rather than equals so 1000.0 and 1000.00 compare as equal
            boolean restrictedDetailsChanged =
                    !Objects.equals(
                            showtime.getMovieId(),
                            request.getMovieId()
                    )
                            || !Objects.equals(
                            showtime.getShowDate(),
                            request.getShowDate()
                    )
                            || !Objects.equals(
                            showtime.getShowTime(),
                            request.getShowTime()
                    )
                            || showtime.getPrice().compareTo(
                            request.getPrice()
                    ) != 0
                            || !Objects.equals(
                            showtime.getSeatsPerRow(),
                            request.getSeatsPerRow()
                    );

            if (restrictedDetailsChanged) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot change movie, date, time, price or row layout " +
                                "after tickets have been confirmed"
                );
            }
        }

        // 7. Protect already reserved seat numbers so shrinking hall capacity never orphans a booked seat
        int highestReservedSeat =
                showtimeRepository.findHighestReservedSeat(id);

        if (request.getTotalSeats() < highestReservedSeat) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot reduce total seats below reserved seat number "
                            + highestReservedSeat
            );
        }

        // 8. Validate overlap against other screenings, excluding this showtime's current slot
        validateNoOverlap(newStart, newEnd, id);

        // 9. Update the existing entity
        showtime.setMovieId(request.getMovieId());
        showtime.setShowDate(request.getShowDate());
        showtime.setShowTime(request.getShowTime());
        showtime.setPrice(request.getPrice());
        showtime.setTotalSeats(request.getTotalSeats());
        showtime.setSeatsPerRow(request.getSeatsPerRow());

        eventPublisher.publishEvent(
                new CatalogRefreshRequestedEvent(showtime.getId())
        );

        // 10. Save and flush immediately to surface DB constraint errors within this transaction
        Showtime updatedShowtime =
                showtimeRepository.saveAndFlush(showtime);

        // 11. Return updated details
        return toResponse(updatedShowtime, movie);
    }

    /**
     * Activates or deactivates a showtime for bookings.
     *
     * <p>Deactivating a past or upcoming showtime is always allowed, whereas reactivating a
     * showtime whose start time has already passed in {@code Asia/Colombo} is rejected.
     *
     * @param id     showtime ID
     * @param active target {@code active} state
     * @return updated {@link ShowtimeResponse}
     * @throws ResourceNotFoundException if the showtime does not exist
     * @throws ResponseStatusException   with HTTP 400 when attempting to reactivate a past showtime
     */
    @Transactional
    public ShowtimeResponse updateShowtimeStatus(
            Long id,
            boolean active
    ) {

        // Step 1: Find the existing showtime
        Showtime showtime = showtimeRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with id: " + id
                ));

        // Step 2: Short-circuit idempotent requests without issuing an unnecessary UPDATE
        if (showtime.isActive() == active) {
            return toResponse(showtime);
        }

        // Step 3: Prevent reactivating past showtimes
        if (active) {

            LocalDateTime startDateTime = LocalDateTime.of(
                    showtime.getShowDate(),
                    showtime.getShowTime()
            );

            if (!startDateTime.isAfter(
                    LocalDateTime.now(CINEMA_TIME_ZONE)
            )) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot reactivate a showtime that has already started"
                );
            }
        }

        // Step 4: Update the active status
        showtime.setActive(active);

        // Step 5: Save the updated showtime
        Showtime updatedShowtime =
                showtimeRepository.saveAndFlush(showtime);

        eventPublisher.publishEvent(
                new CatalogRefreshRequestedEvent(showtime.getId())
        );

        // Step 6: Return the updated details
        return toResponse(updatedShowtime);
    }

    /**
     * Computes live seat inventory for a showtime by subtracting committed reserved seats
     * ({@code CONFIRMED} or {@code PAID}) from {@code totalSeats}.
     *
     * @param showtimeId target showtime ID
     * @return seat capacity, layout, sorted reserved seat numbers, remaining available count, and active flag
     * @throws ResourceNotFoundException if the showtime does not exist
     */
    @Transactional(readOnly = true)
    public ShowtimeSeatAvailabilityResponse getSeatAvailability(
            Long showtimeId
    ) {

        // Step 1: Find the requested showtime
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with id: " + showtimeId
                ));

        // Step 2: Retrieve reserved seat numbers from committed reservations
        List<Integer> reservedSeats =
                showtimeRepository.findReservedSeatNumbers(showtimeId);

        // Step 3: Get the total seat capacity
        int totalSeats = showtime.getTotalSeats();

        // Step 4: Count reserved seats
        int reservedCount = reservedSeats.size();

        // Step 5: Calculate unreserved seats
        int availableSeats = totalSeats - reservedCount;

        // Step 6: Build the API response
        return new ShowtimeSeatAvailabilityResponse(
                showtime.getId(),
                totalSeats,
                showtime.getSeatsPerRow(),
                reservedSeats,
                availableSeats,
                showtime.isActive()
        );
    }
}