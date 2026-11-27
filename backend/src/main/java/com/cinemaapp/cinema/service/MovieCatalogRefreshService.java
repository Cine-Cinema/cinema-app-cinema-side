package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.entity.Showtime;
import com.cinemaapp.cinema.kafka.CatalogRefreshRequestedEvent;
import com.cinemaapp.cinema.repository.ShowtimeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Requests catalog refreshes for every showtime
 * associated with a movie.
 *
 * This service does not publish directly to Kafka.
 *
 * Call it from within an existing Spring-managed
 * database transaction.
 */
@Service
public class MovieCatalogRefreshService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    MovieCatalogRefreshService.class
            );

    private final ShowtimeRepository showtimeRepository;

    private final ApplicationEventPublisher eventPublisher;

    public MovieCatalogRefreshService(
            ShowtimeRepository showtimeRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.showtimeRepository = showtimeRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Requests a refresh for every showtime of this movie.
     *
     * The AFTER_COMMIT listener will create the actual
     * catalog snapshot after the caller's transaction commits.
     */
    public void requestRefreshForMovie(Long movieId) {

        List<Showtime> showtimes =
                showtimeRepository
                        .findByMovieIdOrderByShowDateAscShowTimeAsc(
                                movieId
                        );

        for (Showtime showtime : showtimes) {

            eventPublisher.publishEvent(
                    new CatalogRefreshRequestedEvent(
                            showtime.getId()
                    )
            );
        }

        logger.info(
                "Requested catalog refresh for movieId={}, showtimeCount={}",
                movieId,
                showtimes.size()
        );
    }
}