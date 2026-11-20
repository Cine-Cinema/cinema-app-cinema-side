package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // GET /api/movies
    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    // GET /api/movies/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {

        Optional<Movie> movie = movieService.getMovieById(id);

        if (movie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(movie.get());
    }

    // POST /api/movies
    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody Movie movie) {

        Movie savedMovie = movieService.createMovie(movie);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
    }

    // PUT /api/movies/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(
            @PathVariable Long id,
            @RequestBody Movie movieDetails) {

        Optional<Movie> updatedMovie = movieService.updateMovie(id, movieDetails);

        if (updatedMovie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedMovie.get());
    }

    // DELETE /api/movies/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        boolean deleted = movieService.deleteMovie(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}