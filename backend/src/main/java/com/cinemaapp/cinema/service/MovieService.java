package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.MovieRequest;
import com.cinemaapp.cinema.dto.MovieResponse;
import com.cinemaapp.cinema.entity.Movie;
import com.cinemaapp.cinema.repository.MovieRepository;
import org.springframework.stereotype.Service;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.repository.MovieImageRepository;

import java.util.List;

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

    // Retrieve all movies
    public List<MovieResponse> getAllMovies() {

        return movieRepository.findAll()
                .stream()
                .map(this::toResponse)
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

        return toResponse(movie);
    }

    // Create a new movie
    public MovieResponse createMovie(MovieRequest request) {

        Movie movie = new Movie(
                request.getTitle(),
                request.getDescription(),
                request.getDurationMinutes()
        );

        Movie savedMovie = movieRepository.save(movie);

        return toResponse(savedMovie);
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

        return toResponse(updatedMovie);
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

    // Convert Movie Entity into MovieResponse DTO
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