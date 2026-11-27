package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.entity.Cinema;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.entity.Showtime;

import com.cinemaapp.cinema.repository.CinemaRepository;
import com.cinemaapp.cinema.repository.MovieRepository;
import com.cinemaapp.cinema.repository.MovieImageRepository;
import com.cinemaapp.cinema.repository.ShowtimeRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Builds complete catalog snapshots from the Cinema database.
 *
 * The JSON field names match the approved catalog.json contract.
 *
 * This service does not publish Kafka messages.
 *
 * It can be reused by:
 * 1. Kafka catalog publishing
 * 2. Future gRPC ListShowtimes synchronization
 */
@Service
public class CatalogSnapshotService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final CinemaRepository cinemaRepository;
    private final MovieRepository movieRepository;
    private final MovieImageRepository movieImageRepository;
    private final ShowtimeRepository showtimeRepository;

    private final Long cinemaProfileId;

    /**
     * Constructor dependency injection.
     */
    public CatalogSnapshotService(
            CinemaRepository cinemaRepository,
            MovieRepository movieRepository,
            MovieImageRepository movieImageRepository,
            ShowtimeRepository showtimeRepository,
            @Value("${app.catalog.cinema-profile-id}")
            Long cinemaProfileId
    ) {

        this.cinemaRepository = cinemaRepository;
        this.movieRepository = movieRepository;
        this.movieImageRepository = movieImageRepository;
        this.showtimeRepository = showtimeRepository;
        this.cinemaProfileId = cinemaProfileId;
    }

    /**
     * Builds one complete snapshot using committed database state.
     *
     * REQUIRES_NEW creates a separate transaction when this method
     * is called from an AFTER_COMMIT event listener.
     *
     * The method performs read-only database operations.
     */
    @Transactional(
            readOnly = true,
            propagation = Propagation.REQUIRES_NEW
    )
    public Map<String, Object> buildSnapshot(Long showtimeId) {

        // 1. Retrieve the showtime.
        Showtime showtime = showtimeRepository
                .findById(showtimeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Showtime not found: " + showtimeId
                ));

        // 2. Retrieve the associated movie.
        Movie movie = movieRepository
                .findById(showtime.getMovieId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Movie not found: " + showtime.getMovieId()
                ));

        // 3. Retrieve the configured cinema profile.
        Cinema cinema = cinemaRepository
                .findById(cinemaProfileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Configured cinema profile not found: "
                                + cinemaProfileId
                ));

        // 4. Check whether a poster exists.
        boolean hasPoster = movieImageRepository.existsById(
                movie.getId()
        );

        // 5. Create a public, relative poster URL if needed.
        String posterUrl = hasPoster
                ? "/api/movies/" + movie.getId() + "/poster"
                : null;

        // 6. Retrieve reserved seats.
        //
        // The existing repository query includes CONFIRMED
        // and PAID reservations.
        List<Integer> reservedSeats = new ArrayList<>(
                new TreeSet<>(
                        showtimeRepository.findReservedSeatNumbers(
                                showtime.getId()
                        )
                )
        );

        // 7. Calculate available seats.
        int availableSeats =
                showtime.getTotalSeats() - reservedSeats.size();

        // 8. Prepare the approved catalog JSON fields.
        //
        // LinkedHashMap preserves insertion order.
        // It also permits null values, such as posterUrl.
        Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("cinemaName", cinema.getName());

        snapshot.put("movieId", movie.getId());

        snapshot.put("title", movie.getTitle());

        snapshot.put("description", movie.getDescription());

        snapshot.put("posterUrl", posterUrl);

        snapshot.put("showtimeId", showtime.getId());

        snapshot.put(
                "showDate",
                showtime.getShowDate().toString()
        );

        snapshot.put(
                "showTime",
                showtime.getShowTime().format(TIME_FORMAT)
        );

        snapshot.put("price", showtime.getPrice());

        snapshot.put("totalSeats", showtime.getTotalSeats());

        snapshot.put("seatsPerRow", showtime.getSeatsPerRow());

        snapshot.put("reservedSeats", reservedSeats);

        snapshot.put("availableSeats", availableSeats);

        snapshot.put("active", showtime.isActive());

        // 9. Return the completed snapshot.
        return snapshot;
    }
}