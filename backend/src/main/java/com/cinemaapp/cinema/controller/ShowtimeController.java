package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.ShowtimeRequest;
import com.cinemaapp.cinema.dto.ShowtimeResponse;
import com.cinemaapp.cinema.service.ShowtimeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import com.cinemaapp.cinema.dto.UpdateShowtimeStatusRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import com.cinemaapp.cinema.dto.ShowtimeSeatAvailabilityResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Protected REST controller for scheduling, querying, updating, toggling, and inspecting
 * seat availability of movie showtimes.
 *
 * <p>All endpoints under {@code /api/showtimes} require JWT authentication and delegate
 * validation and business rules to {@link ShowtimeService}.
 */
@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    /**
     * Schedules a new showtime for an existing movie.
     *
     * @param request validated showtime details (movie ID, date, start time, price, seating)
     * @return HTTP 201 Created with the scheduled {@link ShowtimeResponse}
     */
    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(
            @Valid @RequestBody ShowtimeRequest request
    ) {

        ShowtimeResponse response =
                showtimeService.createShowtime(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Lists showtimes, optionally filtered by ISO date ({@code yyyy-MM-dd}) and/or movie ID.
     *
     * <p>{@link DateTimeFormat} ensures query strings like {@code ?date=2026-12-01} parse
     * cleanly into a {@link LocalDate}.
     *
     * @param date    optional screening date filter
     * @param movieId optional movie ID filter
     * @return HTTP 200 OK with chronologically ordered {@link ShowtimeResponse} items
     */
    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> getAllShowtimes(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            Long movieId

    ) {

        List<ShowtimeResponse> response =
                showtimeService.getAllShowtimes(date, movieId);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single showtime by its ID.
     *
     * @param id showtime ID from the URL path
     * @return HTTP 200 OK with {@link ShowtimeResponse}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getShowtimeById(
            @PathVariable Long id
    ) {

        ShowtimeResponse response =
                showtimeService.getShowtimeById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing showtime's details, subject to reservation-protection and overlap rules.
     *
     * @param id      showtime ID from the URL path
     * @param request validated replacement showtime values
     * @return HTTP 200 OK with the updated {@link ShowtimeResponse}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> updateShowtime(
            @PathVariable Long id,
            @Valid @RequestBody ShowtimeRequest request
    ) {

        ShowtimeResponse response =
                showtimeService.updateShowtime(id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Partially updates only the {@code active} status of a showtime ({@code PATCH}).
     *
     * @param id      showtime ID from the URL path
     * @param request validated payload containing the desired boolean {@code active} flag
     * @return HTTP 200 OK with the updated {@link ShowtimeResponse}
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ShowtimeResponse> updateShowtimeStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShowtimeStatusRequest request
    ) {

        ShowtimeResponse response =
                showtimeService.updateShowtimeStatus(
                        id,
                        request.getActive()
                );

        return ResponseEntity.ok(response);
    }

    /**
     * Returns live seat capacity, layout, reserved seat numbers, and remaining available seats
     * for a specific showtime.
     *
     * @param id showtime ID from the URL path
     * @return HTTP 200 OK with {@link ShowtimeSeatAvailabilityResponse}
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<ShowtimeSeatAvailabilityResponse> getSeatAvailability(
            @PathVariable Long id
    ) {

        ShowtimeSeatAvailabilityResponse response =
                showtimeService.getSeatAvailability(id);

        return ResponseEntity.ok(response);
    }

}