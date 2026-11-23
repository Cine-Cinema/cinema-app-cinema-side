package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.ShowtimeRequest;
import com.cinemaapp.cinema.dto.ShowtimeResponse;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.entity.Showtime;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.repository.MovieRepository;
import com.cinemaapp.cinema.repository.ShowtimeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ShowtimeService {

    private static final ZoneId CINEMA_TIME_ZONE =
            ZoneId.of("Asia/Colombo");

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    public ShowtimeService(
            ShowtimeRepository showtimeRepository,
            MovieRepository movieRepository
    ) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    // Create a new showtime
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

        // Step 4: Calculate screening end time
        LocalDateTime newEnd = newStart.plusMinutes(
                movie.getDurationMinutes()
        );

        // Step 5: Reject screenings scheduled in the past
        if (!newStart.isAfter(
                LocalDateTime.now(CINEMA_TIME_ZONE)
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Showtime must be scheduled in the future"
            );
        }

        // Step 6: Check overlapping screenings
        validateNoOverlap(newStart, newEnd);

        // Step 7: Create the Showtime entity
        Showtime showtime = new Showtime(
                movie.getId(),
                request.getShowDate(),
                request.getShowTime(),
                request.getPrice(),
                request.getTotalSeats(),
                request.getSeatsPerRow()
        );

        // Step 8: Save the showtime
        Showtime savedShowtime =
                showtimeRepository.saveAndFlush(showtime);

        // Step 9: Return the response
        return toResponse(savedShowtime, movie);
    }

    // Validate seat configuration
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

    // Check for overlapping screenings
    private void validateNoOverlap(
            LocalDateTime newStart,
            LocalDateTime newEnd
    ) {

        List<Showtime> existingShowtimes =
                showtimeRepository.findAll();

        for (Showtime existing : existingShowtimes) {

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

    // Convert Showtime entity into response DTO
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
}