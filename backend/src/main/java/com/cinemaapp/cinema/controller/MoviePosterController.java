package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.MoviePosterResponse;
import com.cinemaapp.cinema.service.MoviePosterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cinemaapp.cinema.entity.MovieImage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * REST controller managing movie poster uploads, replacements, deletions, and public retrieval.
 *
 * <p>Write operations ({@code POST}, {@code PUT}, {@code DELETE}) require authentication,
 * whereas {@code GET /api/movies/{movieId}/poster} is permitted publicly in
 * {@link com.cinemaapp.cinema.config.SecurityConfig} so browsers can load poster images directly.
 */
@RestController
@RequestMapping("/api/movies")
public class MoviePosterController {

    private final MoviePosterService moviePosterService;

    public MoviePosterController(MoviePosterService moviePosterService) {
        this.moviePosterService = moviePosterService;
    }

    /**
     * Uploads an initial poster image for a movie via {@code multipart/form-data}.
     *
     * @param movieId target movie ID
     * @param file    uploaded image file under form part name {@code "file"}
     * @return HTTP 201 Created with poster metadata and download URL
     */
    @PostMapping(
            value = "/{movieId}/poster",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MoviePosterResponse> uploadPoster(
            @PathVariable Long movieId,
            @RequestParam("file") MultipartFile file
    ) {

        MoviePosterResponse response =
                moviePosterService.uploadPoster(movieId, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Replaces an existing poster image for a movie via {@code multipart/form-data}.
     *
     * @param movieId target movie ID
     * @param file    replacement image file under form part name {@code "file"}
     * @return HTTP 200 OK with updated poster metadata
     */
    @PutMapping(
            value = "/{movieId}/poster",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MoviePosterResponse> replacePoster(
            @PathVariable Long movieId,
            @RequestParam("file") MultipartFile file
    ) {

        MoviePosterResponse response =
                moviePosterService.replacePoster(movieId, file);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the poster image associated with a movie.
     *
     * @param movieId target movie ID
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{movieId}/poster")
    public ResponseEntity<Void> deletePoster(
            @PathVariable Long movieId
    ) {

        moviePosterService.deletePoster(movieId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Publicly streams the raw poster image bytes stored in PostgreSQL.
     *
     * <p>Sets the HTTP {@code Content-Type} header from the verified image format
     * ({@code image/jpeg}, {@code image/png}, or {@code image/webp}) and {@code Content-Length}
     * so browsers and {@code <img>} elements render the binary payload directly.
     *
     * @param movieId target movie ID
     * @return HTTP 200 OK with raw image bytes
     */
    @GetMapping("/{movieId}/poster")
    public ResponseEntity<byte[]> getPoster(
            @PathVariable Long movieId
    ) {

        // Retrieve the image from PostgreSQL
        MovieImage movieImage = moviePosterService.getPoster(movieId);

        // Return the actual image bytes with matching Content-Type and Content-Length headers
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(movieImage.getContentType())
                )
                .contentLength(movieImage.getData().length)
                .body(movieImage.getData());
    }
}