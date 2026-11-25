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
 * Service handling validation, binary PostgreSQL storage, retrieval, replacement,
 * and deletion of movie poster images.
 *
 * <p>Instead of trusting client-supplied file extensions or {@code Content-Type} headers,
 * this service inspects the actual image bytes using Java's {@link ImageIO} (plus the
 * {@code webp-imageio} plugin) to verify format, dimensions, and decodability.
 */
@Service
public class MoviePosterService {

    /** Maximum permitted poster payload size (2 MiB), aligned with multipart settings. */
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024;

    /** Dimension and total-pixel ceilings checked before decoding to prevent memory-exhaustion (image bomb) attacks. */
    private static final int MAX_IMAGE_WIDTH = 6000;
    private static final int MAX_IMAGE_HEIGHT = 6000;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;

    private final MovieRepository movieRepository;
    private final MovieImageRepository movieImageRepository;

    /** Internal immutable holder pairing validated raw bytes with their detected MIME type. */
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

    /**
     * Validates that a multipart upload is non-empty, within 2 MiB, and contains a valid
     * JPEG, PNG, or WebP image.
     */
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

        // Read the uploaded file bytes into memory
        byte[] imageData;

        try {
            imageData = file.getBytes();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read the uploaded image"
            );
        }

        // Detect and validate the actual image format from the binary header/contents
        String contentType = validateAndDetectImage(imageData);

        return new ValidatedPoster(imageData, contentType);
    }

    /**
     * Uploads a new poster for a movie that does not already have one.
     *
     * @param movieId target movie ID
     * @param file    multipart image upload (JPEG, PNG, or WebP, max 2 MiB)
     * @return metadata describing the stored poster and its public download URL
     * @throws ResourceNotFoundException if the movie does not exist
     * @throws ResponseStatusException   with HTTP 409 Conflict if the movie already has a poster
     *                                   (use {@link #replacePoster} instead), or 400 if invalid
     */
    @Transactional
    public MoviePosterResponse uploadPoster(
            Long movieId,
            MultipartFile file
    ) {

        // Check whether the movie exists before reading/validating the file
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException(
                    "Movie not found with id: " + movieId
            );
        }

        // Validate uploaded image bytes and detect MIME type
        ValidatedPoster validated = validateUploadedPoster(file);

        // Enforce 1-to-1 poster ownership on POST so accidental overwrites are avoided
        if (movieImageRepository.existsById(movieId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This movie already has a poster"
            );
        }

        // Create poster entity keyed by the movie's ID
        MovieImage movieImage = new MovieImage(
                movieId,
                validated.contentType(),
                validated.data()
        );

        // Save binary poster into PostgreSQL (BYTEA)
        movieImageRepository.save(movieImage);

        // Return poster metadata response
        return new MoviePosterResponse(
                movieId,
                validated.contentType(),
                "/api/movies/" + movieId + "/poster",
                validated.data().length
        );
    }

    /**
     * Replaces an existing movie poster with a newly validated image.
     *
     * @param movieId target movie ID
     * @param file    replacement multipart image upload
     * @return updated poster metadata
     * @throws ResourceNotFoundException if the movie or its existing poster does not exist
     */
    @Transactional
    public MoviePosterResponse replacePoster(
            Long movieId,
            MultipartFile file
    ) {

        // Reuse getPoster to ensure both the movie and an existing poster exist
        MovieImage movieImage = getPoster(movieId);

        // Validate the replacement image
        ValidatedPoster validated = validateUploadedPoster(file);

        // Replace the previous image details; @PreUpdate will refresh updatedAt on save
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

    /**
     * Retrieves the stored {@link MovieImage} entity (including binary bytes and content type)
     * for public serving.
     *
     * @param movieId target movie ID
     * @return persisted {@link MovieImage}
     * @throws ResourceNotFoundException if either the movie or its poster is missing
     */
    @Transactional(readOnly = true)
    public MovieImage getPoster(Long movieId) {

        // Check whether the movie exists first so the error clearly distinguishes missing movie vs missing poster
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

    /**
     * Deletes the poster belonging to a movie.
     *
     * @param movieId target movie ID
     * @throws ResourceNotFoundException if the movie or its poster does not exist
     */
    @Transactional
    public void deletePoster(Long movieId) {

        // Verify both movie and poster exist before deleting
        MovieImage movieImage = getPoster(movieId);

        // Delete the poster row from movie_image
        movieImageRepository.delete(movieImage);
    }

    /**
     * Inspects raw bytes with {@link ImageIO} to determine the true image format, enforce
     * safe dimension bounds, and confirm the image decodes without corruption.
     */
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

            // Reject files that no supported ImageIO reader recognizes (e.g., text or spoofed extensions)
            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();

            try {

                reader.setInput(inputStream, true, true);

                // Identify the actual image format from the file's magic bytes
                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                String contentType = switch (format) {
                    case "jpeg", "jpg" -> "image/jpeg";
                    case "png" -> "image/png";
                    case "webp" -> "image/webp";
                    default -> throw invalidImage();
                };

                // Read header dimensions BEFORE full pixel decoding to block decompression bombs
                // (e.g., a tiny compressed file claiming huge pixel dimensions). Cast to long prevents int overflow.
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

                // Fully decode frame 0 to verify the image payload is not truncated or corrupted
                if (reader.read(0) == null) {
                    throw invalidImage();
                }

                return contentType;

            } finally {
                // Release native ImageReader resources even if validation fails
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