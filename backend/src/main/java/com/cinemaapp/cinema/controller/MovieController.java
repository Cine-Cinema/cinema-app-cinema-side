package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.MovieRequest;
import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // GET /api/movies
    @GetMapping
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovies();
    }

    // GET /api/movies/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {

        MovieResponse movie = movieService.getMovieById(id);

        return ResponseEntity.ok(movie);
    }

    // POST /api/movies
    @PostMapping
    public ResponseEntity<MovieResponse> createMovie(
            @Valid @RequestBody MovieRequest request) {

        MovieResponse savedMovie = movieService.createMovie(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMovie);
    }

    // PUT /api/movies/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request) {

        MovieResponse updatedMovie = movieService.updateMovie(id, request);

        return ResponseEntity.ok(updatedMovie);
    }

    // DELETE /api/movies/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        movieService.deleteMovie(id);

        return ResponseEntity.noContent().build();
    }
}