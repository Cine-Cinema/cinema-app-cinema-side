package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.MoviePosterResponse;
import com.cinemaapp.cinema.entity.MovieImage;
import com.cinemaapp.cinema.exception.ResourceNotFoundException;
import com.cinemaapp.cinema.repository.MovieImageRepository;
import com.cinemaapp.cinema.repository.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

@Service
public class MoviePosterService {

    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;

    private static final int MAX_IMAGE_WIDTH = 6000;
    private static final int MAX_IMAGE_HEIGHT = 6000;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;

    private final MovieRepository movieRepository;
    private final MovieImageRepository movieImageRepository;

    private record ValidatedPoster(
            byte[] data,
            String contentType
    ) {
    }

    public MoviePosterService(
            MovieRepository movieRepository,
            MovieImageRepository movieImageRepository
    ) {
        this.movieRepository = movieRepository;
        this.movieImageRepository = movieImageRepository;
    }

    private ValidatedPoster validateUploadedPoster(MultipartFile file) {

        // Check whether a file was provided
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a poster image"
            );
        }

        // Check maximum size
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Poster image cannot exceed 2 MB"
            );
        }

        // Read the uploaded file
        byte[] imageData;

        try {
            imageData = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read the uploaded image"
            );
        }

        // Detect and validate the actual image format
        String contentType = validateAndDetectImage(imageData);

        return new ValidatedPoster(imageData, contentType);
    }

    @Transactional
    public MoviePosterResponse uploadPoster(
            Long movieId,
            MultipartFile file
    ) {

        // Check whether the movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException(
                    "Movie not found with id: " + movieId
            );
        }

        // Validate uploaded image
        ValidatedPoster validated = validateUploadedPoster(file);

        // Prevent duplicate posters
        if (movieImageRepository.existsById(movieId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This movie already has a poster"
            );
        }

        // Create poster entity
        MovieImage movieImage = new MovieImage(
                movieId,
                validated.contentType(),
                validated.data()
        );

        // Save into PostgreSQL
        movieImageRepository.save(movieImage);

        // Return response
        return new MoviePosterResponse(
                movieId,
                validated.contentType(),
                "/api/movies/" + movieId + "/poster",
                validated.data().length
        );
    }

    @Transactional
    public MoviePosterResponse replacePoster(
            Long movieId,
            MultipartFile file
    ) {

        // Find the existing poster
        MovieImage movieImage = getPoster(movieId);

        // Validate the replacement image
        ValidatedPoster validated = validateUploadedPoster(file);

        // Replace the previous image details
        movieImage.setContentType(validated.contentType());
        movieImage.setData(validated.data());

        // Save the updated entity
        movieImageRepository.save(movieImage);

        // Return updated poster information
        return new MoviePosterResponse(
                movieId,
                validated.contentType(),
                "/api/movies/" + movieId + "/poster",
                validated.data().length
        );
    }

    @Transactional(readOnly = true)
    public MovieImage getPoster(Long movieId) {

        // Check whether the movie exists
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException(
                    "Movie not found with id: " + movieId
            );
        }

        // Find the poster associated with the movie
        return movieImageRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poster not found for movie id: " + movieId
                ));
    }

    @Transactional
    public void deletePoster(Long movieId) {

        // Find the existing poster
        MovieImage movieImage = getPoster(movieId);

        // Delete the poster
        movieImageRepository.delete(movieImage);
    }

    private String validateAndDetectImage(byte[] imageData) {

        try (
                ImageInputStream inputStream =
                        ImageIO.createImageInputStream(
                                new ByteArrayInputStream(imageData)
                        )
        ) {

            if (inputStream == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(inputStream);

            // Reject files that no supported image reader recognizes
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();

            try {

                reader.setInput(inputStream, true, true);

                // Identify the actual image format
                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                String contentType = switch (format) {
                    case "jpeg", "jpg" -> "image/jpeg";
                    case "png" -> "image/png";
                    case "webp" -> "image/webp";
                    default -> throw invalidImage();
                };

                // Check image dimensions before decoding
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0 ||
                        width > MAX_IMAGE_WIDTH ||
                        height > MAX_IMAGE_HEIGHT ||
                        (long) width * height > MAX_IMAGE_PIXELS) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Poster dimensions are too large"
                    );
                }

                // Actually decode the image to verify it is readable
                if (reader.read(0) == null) {
                    throw invalidImage();
                }

                return contentType;

            } finally {
                reader.dispose();
            }

        } catch (IOException exception) {

            throw invalidImage();
        }
    }

    private ResponseStatusException invalidImage() {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid image. Only valid JPEG, PNG and WebP images are allowed."
        );
    }
}