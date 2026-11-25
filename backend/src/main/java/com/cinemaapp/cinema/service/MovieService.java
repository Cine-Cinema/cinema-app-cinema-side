package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.MovieRequest;
import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.repository.MovieRepository;
import org.springframework.stereotype.Service;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.repository.MovieImageRepository;

import java.util.List;

/**
 * Service layer managing CRUD operations for the cinema's movie catalog.
 *
 * <p>Coordinates {@link MovieRepository} for movie metadata and {@link MovieImageRepository}
 * to enrich responses with a {@code posterUrl} whenever a movie has an uploaded poster.
 */
@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieImageRepository movieImageRepository;

    public MovieService(
            MovieRepository movieRepository,
            MovieImageRepository movieImageRepository
    ) {
        this.movieRepository = movieRepository;
        this.movieImageRepository = movieImageRepository;
    }

    /**
     * Returns every movie in the catalog, each enriched with its public {@code posterUrl} if present.
     *
     * @return list of all movies as {@link MovieResponse} DTOs
     */
    public List<MovieResponse> getAllMovies() {

        return movieRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Fetches a single movie by its ID.
     *
     * @param id movie primary key
     * @return matching {@link MovieResponse}
     * @throws ResourceNotFoundException if no movie exists with {@code id}
     */
    public MovieResponse getMovieById(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: " + id
                        )
                );

        return toResponse(movie);
    }

    /**
     * Persists a new movie in PostgreSQL.
     *
     * @param request validated movie details (title, optional description, duration in minutes)
     * @return created {@link MovieResponse} with generated ID
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
     * Updates an existing movie's title, description, and duration.
     *
     * @param id      ID of the movie to update
     * @param request validated replacement values
     * @return updated {@link MovieResponse}
     * @throws ResourceNotFoundException if no movie exists with {@code id}
     */
    public MovieResponse updateMovie(Long id, MovieRequest request) {

        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: " + id
                        )
                );

        existingMovie.setTitle(request.getTitle());
        existingMovie.setDescription(request.getDescription());
        existingMovie.setDurationMinutes(request.getDurationMinutes());

        Movie updatedMovie = movieRepository.save(existingMovie);

        return toResponse(updatedMovie);
    }

    /**
     * Deletes a movie by ID if it exists.
     *
     * <p>Note: if the movie still has a poster in {@code movie_image} or showtimes in
     * {@code showtime}, PostgreSQL foreign-key constraints will reject deletion and
     * {@code GlobalExceptionHandler} will return HTTP 409 Conflict.
     *
     * @param id ID of the movie to delete
     * @throws ResourceNotFoundException if no movie exists with {@code id}
     */
    public void deleteMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: " + id
                        )
                );

        movieRepository.delete(movie);
    }

    /**
     * Returns the relative public poster endpoint path only if a poster exists in {@code movie_image};
     * uses {@code existsById} to avoid loading the binary {@code BYTEA} column.
     */
    private String buildPosterUrl(Long movieId) {

        boolean posterExists = movieImageRepository.existsById(movieId);

        if (!posterExists) {
            return null;
        }

        return "/api/movies/" + movieId + "/poster";
    }

    private MovieResponse attachPosterUrl(MovieResponse response) {

        String posterUrl = buildPosterUrl(response.getId());

        response.setPosterUrl(posterUrl);

        return response;
    }

    /** Maps a {@link Movie} entity to {@link MovieResponse} and attaches its {@code posterUrl}. */
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