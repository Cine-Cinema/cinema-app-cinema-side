package com.cinemaapp.cinema.dto;

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