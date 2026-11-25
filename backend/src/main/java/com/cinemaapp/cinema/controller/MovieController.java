package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.MovieRequest;
import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Protected REST controller providing CRUD endpoints for the cinema's movie catalog.
 *
 * <p>Delegates all business rules and persistence to {@link MovieService}. All endpoints
 * under {@code /api/movies} require authentication in {@code SecurityConfig} (only
 * {@code GET /api/movies/{movieId}/poster} in {@link MoviePosterController} is public).
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Lists all movies in the catalog along with their {@code posterUrl} when a poster exists.
     *
     * @return list of all {@link MovieResponse} records
     */
    @GetMapping
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }

    /**
     * Retrieves a single movie by its ID.
     *
     * @param id movie primary key from the URL path
     * @return HTTP 200 OK with {@link MovieResponse}, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {

        MovieResponse movie = movieService.getMovieById(id);

        return ResponseEntity.ok(movie);
    }

    /**
     * Creates a new movie catalog entry.
     *
     * @param request validated movie payload
     * @return HTTP 201 Created with the saved {@link MovieResponse}
     */
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {

        MovieResponse savedMovie = movieService.createMovie(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
    }

    /**
     * Updates an existing movie's metadata.
     *
     * @param id      movie ID from the URL path
     * @param request validated replacement movie payload
     * @return HTTP 200 OK with the updated {@link MovieResponse}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {

        MovieResponse updatedMovie = movieService.updateMovie(id, request);

        return ResponseEntity.ok(updatedMovie);
    }

    /**
     * Deletes a movie by ID.
     *
     * @param id movie ID from the URL path
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        movieService.deleteMovie(id);

        return ResponseEntity.noContent().build();
    }
}