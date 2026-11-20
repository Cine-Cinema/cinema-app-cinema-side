package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.MovieRequest;
import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.repository.MovieRepository;
import org.springframework.stereotype.Service;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Retrieve all movies
    public List<MovieResponse> getAllMovies() {

        return movieRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Retrieve one movie by ID
    public MovieResponse getMovieById(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: " + id
                        )
                );

        return convertToResponse(movie);
    }

    // Create a new movie
    public MovieResponse createMovie(MovieRequest request) {

        Movie movie = new Movie(
                request.getTitle(),
                request.getDescription(),
                request.getDurationMinutes()
        );

        Movie savedMovie = movieRepository.save(movie);

        return convertToResponse(savedMovie);
    }

    // Update an existing movie
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

        return convertToResponse(updatedMovie);
    }

    // Delete a movie
    public void deleteMovie(Long id) {

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Movie not found with id: " + id
                        )
                );

        movieRepository.delete(movie);
    }

    // Convert Movie Entity into MovieResponse DTO
    private MovieResponse convertToResponse(Movie movie) {

        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes()
        );
    }
}