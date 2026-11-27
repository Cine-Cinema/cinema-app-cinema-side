package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.MovieRequest;
import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.repository.MovieImageRepository;
import com.cinemaapp.cinema.repository.MovieRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer managing CRUD operations for the cinema's movie catalog.
 *
 * Coordinates MovieRepository for movie metadata and MovieImageRepository
 * to enrich responses with posterUrl whenever a movie has an uploaded poster.
 *
 * All managers operate on the same shared movie catalog.
 */
@Service
public class MovieService {

    private final MovieRepository movieRepository;

    private final MovieImageRepository movieImageRepository;

    private final MovieCatalogRefreshService movieCatalogRefreshService;

    /**
     * Constructor dependency injection.
     */
    public MovieService(
            MovieRepository movieRepository,
            MovieImageRepository movieImageRepository,
            MovieCatalogRefreshService movieCatalogRefreshService
    ) {
        this.movieRepository = movieRepository;
        this.movieImageRepository = movieImageRepository;
        this.movieCatalogRefreshService = movieCatalogRefreshService;
    }

    /**
     * Returns all movies with their poster URLs, if present.
     */
    public List<MovieResponse> getAllMovies() {

        return movieRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves a movie or throws 404 if it does not exist.
     */
    public MovieResponse getMovieById(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + id
                ));

        return toResponse(movie);
    }

    /**
     * Creates movie metadata.
     *
     * No catalog snapshot is needed until the movie has a showtime.
     */
    public MovieResponse createMovie(MovieRequest request) {

        Movie movie = new Movie(
                request.getTitle(),
                request.getDescription(),
                request.getDurationMinutes()
        );

        Movie savedMovie = movieRepository.save(movie);

        return toResponse(savedMovie);
    }

    /**
     * Updates movie metadata and requests catalog refresh
     * for ALL related showtimes.
     *
     * The helper must run inside this transaction so
     * its Spring events can be handled AFTER_COMMIT.
     */
    @Transactional
    public MovieResponse updateMovie(
            Long id,
            MovieRequest request
    ) {

        // STEP 1: Find the existing movie.
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + id
                ));

        // STEP 2: Update its information.
        existingMovie.setTitle(request.getTitle());

        existingMovie.setDescription(request.getDescription());

        existingMovie.setDurationMinutes(
                request.getDurationMinutes()
        );

        // STEP 3: Save the movie.
        Movie updatedMovie =
                movieRepository.save(existingMovie);

        // STEP 4: Request catalog refreshes for every showtime
        // associated with this movie.
        //
        // The AFTER_COMMIT listener will publish the
        // updated snapshots after the transaction commits.
        movieCatalogRefreshService.requestRefreshForMovie(
                updatedMovie.getId()
        );

        // STEP 5: Return the updated movie.
        return toResponse(updatedMovie);
    }

    /**
     * Deletes a movie.
     *
     * Existing database foreign-key constraints protect
     * movies referenced by posters or showtimes.
     */
    public void deleteMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Movie not found with id: " + id
                ));

        movieRepository.delete(movie);
    }

    /**
     * Returns the public relative poster URL
     * if a poster exists.
     */
    private String buildPosterUrl(Long movieId) {

        boolean posterExists =
                movieImageRepository.existsById(movieId);

        if (!posterExists) {
            return null;
        }

        return "/api/movies/" + movieId + "/poster";
    }

    /**
     * Adds the poster URL to the MovieResponse.
     */
    private MovieResponse attachPosterUrl(
            MovieResponse response
    ) {

        String posterUrl =
                buildPosterUrl(response.getId());

        response.setPosterUrl(posterUrl);

        return response;
    }

    /**
     * Converts a Movie entity into MovieResponse.
     */
    private MovieResponse toResponse(Movie movie) {

        MovieResponse response = new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes()
        );

        return attachPosterUrl(response);
    }
}