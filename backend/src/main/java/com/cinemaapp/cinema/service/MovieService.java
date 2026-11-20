package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Retrieve all movies
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Retrieve one movie by ID
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    // Create a new movie
    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // Update an existing movie
    public Optional<Movie> updateMovie(Long id, Movie movieDetails) {

        Optional<Movie> optionalMovie = movieRepository.findById(id);

        if (optionalMovie.isEmpty()) {
            return Optional.empty();
        }

        Movie existingMovie = optionalMovie.get();

        existingMovie.setTitle(movieDetails.getTitle());
        existingMovie.setDescription(movieDetails.getDescription());
        existingMovie.setDurationMinutes(movieDetails.getDurationMinutes());

        Movie updatedMovie = movieRepository.save(existingMovie);

        return Optional.of(updatedMovie);
    }

    // Delete a movie
    public boolean deleteMovie(Long id) {

        if (!movieRepository.existsById(id)) {
            return false;
        }

        movieRepository.deleteById(id);

        return true;
    }
}