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

@RestController
@RequestMapping("/api/movies")
public class MoviePosterController {

    private final MoviePosterService moviePosterService;

    public MoviePosterController(MoviePosterService moviePosterService) {
        this.moviePosterService = moviePosterService;
    }

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

    @DeleteMapping("/{movieId}/poster")
    public ResponseEntity<Void> deletePoster(
            @PathVariable Long movieId
    ) {

        moviePosterService.deletePoster(movieId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{movieId}/poster")
    public ResponseEntity<byte[]> getPoster(
            @PathVariable Long movieId
    ) {

        // Retrieve the image from PostgreSQL
        MovieImage movieImage = moviePosterService.getPoster(movieId);

        // Return the actual image bytes
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(movieImage.getContentType())
                )
                .contentLength(movieImage.getData().length)
                .body(movieImage.getData());
    }
}