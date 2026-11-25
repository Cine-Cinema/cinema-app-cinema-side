package com.cinemaapp.cinema.dto;

/**
 * Response DTO returned after uploading or replacing a movie poster image.
 *
 * <p>Returns poster metadata and the public {@code posterUrl} rather than echoing back
 * the binary image payload.
 */
public class MoviePosterResponse {

    private final Long movieId;
    private final String contentType;
    private final String posterUrl;
    private final long sizeBytes;

    public MoviePosterResponse(
            Long movieId,
            String contentType,
            String posterUrl,
            long sizeBytes
    ) {
        this.movieId = movieId;
        this.contentType = contentType;
        this.posterUrl = posterUrl;
        this.sizeBytes = sizeBytes;
    }

    public Long getMovieId() {
        return movieId;
    }

    public String getContentType() {
        return contentType;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}