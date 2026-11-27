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

/**
 * Service handling validation, binary PostgreSQL storage,
 * retrieval, replacement, and deletion of movie poster images.
 *
 * The service validates actual image bytes rather than trusting
 * file extensions or Content-Type headers.
 *
 * It also requests Kafka catalog refreshes whenever poster
 * information changes successfully.
 */
@Service
public class MoviePosterService {

    /**
     * Maximum permitted poster payload: 2 MiB.
     */
    private static final long MAX_IMAGE_SIZE =
            2 * 1024 * 1024;

    /**
     * Image dimension limits.
     *
     * These help protect the application against
     * excessively large or malicious images.
     */
    private static final int MAX_IMAGE_WIDTH = 6000;

    private static final int MAX_IMAGE_HEIGHT = 6000;

    private static final long MAX_IMAGE_PIXELS = 20_000_000L;

    private final MovieRepository movieRepository;

    private final MovieImageRepository movieImageRepository;

    // NEW: Requests catalog refreshes after poster changes.
    private final MovieCatalogRefreshService movieCatalogRefreshService;

    /**
     * Internal record containing validated poster data.
     */
    private record ValidatedPoster(
            byte[] data,
            String contentType
    ) {
    }

    /**
     * Constructor dependency injection.
     */
    public MoviePosterService(
            MovieRepository movieRepository,
            MovieImageRepository movieImageRepository,
            MovieCatalogRefreshService movieCatalogRefreshService
    ) {

        this.movieRepository = movieRepository;

        this.movieImageRepository = movieImageRepository;

        this.movieCatalogRefreshService = movieCatalogRefreshService;
    }

    /**
     * Validates an uploaded poster.
     *
     * Requirements:
     * 1. File must exist.
     * 2. File must not be empty.
     * 3. Maximum size is 2 MiB.
     * 4. Must be a valid JPEG, PNG, or WebP image.
     */
    private ValidatedPoster validateUploadedPoster(
            MultipartFile file
    ) {

        // STEP 1: Check whether a file was provided.
        if (file == null || file.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a poster image"
            );
        }

        // STEP 2: Validate file size.
        if (file.getSize() > MAX_IMAGE_SIZE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Poster image cannot exceed 2 MB"
            );
        }

        // STEP 3: Read image bytes.
        byte[] imageData;

        try {

            imageData = file.getBytes();

        } catch (IOException exception) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read the uploaded image"
            );
        }

        // STEP 4: Detect and validate the actual image format.
        String contentType = validateAndDetectImage(imageData);

        return new ValidatedPoster(
                imageData,
                contentType
        );
    }

    /**
     * Uploads a new poster for a movie.
     *
     * Existing posters cannot be overwritten through this method.
     * Use replacePoster() instead.
     *
     * After successful saving, all showtimes associated
     * with the movie receive catalog refresh requests.
     */
    @Transactional
    public MoviePosterResponse uploadPoster(
            Long movieId,
            MultipartFile file
    ) {

        // STEP 1: Verify that the movie exists.
        if (!movieRepository.existsById(movieId)) {

            throw new ResourceNotFoundException(
                    "Movie not found with id: " + movieId
            );
        }

        // STEP 2: Validate the uploaded file.
        ValidatedPoster validated =
                validateUploadedPoster(file);

        // STEP 3: Prevent accidental overwriting.
        if (movieImageRepository.existsById(movieId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This movie already has a poster"
            );
        }

        // STEP 4: Create a new poster entity.
        MovieImage movieImage = new MovieImage(
                movieId,
                validated.contentType(),
                validated.data()
        );

        // STEP 5: Save poster bytes into PostgreSQL.
        movieImageRepository.save(movieImage);

        // STEP 6: NEW - Request catalog refreshes.
        //
        // The AFTER_COMMIT listener publishes the new
        // posterUrl information after the transaction commits.
        movieCatalogRefreshService.requestRefreshForMovie(
                movieId
        );

        // STEP 7: Return poster metadata.
        return new MoviePosterResponse(
                movieId,
                validated.contentType(),
                "/api/movies/" + movieId + "/poster",
                validated.data().length
        );
    }

    /**
     * Replaces an existing poster.
     *
     * Preserves the movie ID while updating
     * the image bytes and content type.
     *
     * Requests a catalog refresh after successful saving.
     */
    @Transactional
    public MoviePosterResponse replacePoster(
            Long movieId,
            MultipartFile file
    ) {

        // STEP 1: Retrieve existing poster.
        //
        // getPoster() also verifies that the movie exists.
        MovieImage movieImage = getPoster(movieId);

        // STEP 2: Validate the replacement image.
        ValidatedPoster validated =
                validateUploadedPoster(file);

        // STEP 3: Replace image information.
        movieImage.setContentType(
                validated.contentType()
        );

        movieImage.setData(
                validated.data()
        );

        // STEP 4: Save the modified poster.
        movieImageRepository.save(movieImage);

        // STEP 5: NEW - Request catalog refreshes.
        //
        // The poster URL may remain unchanged,
        // but the underlying image bytes have changed.
        movieCatalogRefreshService.requestRefreshForMovie(
                movieId
        );

        // STEP 6: Return updated poster metadata.
        return new MoviePosterResponse(
                movieId,
                validated.contentType(),
                "/api/movies/" + movieId + "/poster",
                validated.data().length
        );
    }

    /**
     * Retrieves the stored poster for public serving.
     *
     * Throws ResourceNotFoundException if the movie
     * or its poster does not exist.
     */
    @Transactional(readOnly = true)
    public MovieImage getPoster(Long movieId) {

        // STEP 1: Verify movie existence.
        if (!movieRepository.existsById(movieId)) {

            throw new ResourceNotFoundException(
                    "Movie not found with id: " + movieId
            );
        }

        // STEP 2: Retrieve the image entity.
        return movieImageRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poster not found for movie id: " + movieId
                ));
    }

    /**
     * Deletes the poster belonging to a movie.
     *
     * After deletion, the movie's catalog snapshots
     * should contain posterUrl = null.
     */
    @Transactional
    public void deletePoster(Long movieId) {

        // STEP 1: Retrieve existing poster.
        //
        // This also verifies that the movie exists.
        MovieImage movieImage = getPoster(movieId);

        // STEP 2: Delete poster from PostgreSQL.
        movieImageRepository.delete(movieImage);

        // STEP 3: NEW - Request catalog refreshes.
        //
        // After the transaction commits, the snapshot
        // service will detect that no poster exists.
        movieCatalogRefreshService.requestRefreshForMovie(
                movieId
        );
    }

    /**
     * Inspects raw image bytes.
     *
     * Detects the actual image format and validates
     * dimensions and decodability.
     *
     * Allowed formats:
     * - JPEG
     * - PNG
     * - WebP
     */
    private String validateAndDetectImage(
            byte[] imageData
    ) {

        try (
                ImageInputStream inputStream =
                        ImageIO.createImageInputStream(
                                new ByteArrayInputStream(imageData)
                        )
        ) {

            // STEP 1: Verify that an image input stream exists.
            if (inputStream == null) {

                throw invalidImage();
            }

            // STEP 2: Find a compatible image reader.
            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(inputStream);

            if (!readers.hasNext()) {

                throw invalidImage();
            }

            ImageReader reader = readers.next();

            try {

                reader.setInput(
                        inputStream,
                        true,
                        true
                );

                // STEP 3: Detect actual image format.
                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                String contentType = switch (format) {

                    case "jpeg", "jpg" -> "image/jpeg";

                    case "png" -> "image/png";

                    case "webp" -> "image/webp";

                    default -> throw invalidImage();
                };

                // STEP 4: Read image dimensions.
                int width = reader.getWidth(0);

                int height = reader.getHeight(0);

                // STEP 5: Validate dimensions.
                //
                // Casting to long prevents integer overflow
                // when calculating total pixel count.
                if (
                        width <= 0 ||
                                height <= 0 ||
                                width > MAX_IMAGE_WIDTH ||
                                height > MAX_IMAGE_HEIGHT ||
                                (long) width * height > MAX_IMAGE_PIXELS
                ) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Poster dimensions are too large"
                    );
                }

                // STEP 6: Decode the image.
                //
                // This helps identify truncated or corrupted files.
                if (reader.read(0) == null) {

                    throw invalidImage();
                }

                return contentType;

            } finally {

                // Release ImageReader resources.
                reader.dispose();
            }

        } catch (IOException exception) {

            throw invalidImage();
        }
    }

    /**
     * Creates the standard invalid-image exception.
     */
    private ResponseStatusException invalidImage() {

        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid image. Only valid JPEG, PNG and WebP images are allowed."
        );
    }
}