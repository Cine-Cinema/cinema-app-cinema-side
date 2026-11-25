package com.cinemaapp.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA entity storing a movie's binary poster image in the {@code movie_image} table.
 *
 * <p>Stored in a separate table from {@link Movie} so listing or querying movies does
 * not pull large binary {@code BYTEA} payloads from PostgreSQL unless the poster itself
 * is requested. Uses a shared primary key ({@code movie_id}) for a 1-to-1 relationship
 * with {@link Movie}.
 */
@Entity
@Table(name = "movie_image")
public class MovieImage {

    /**
     * Primary key matching {@link Movie#getId()} (assigned manually rather than auto-generated,
     * since it mirrors the foreign key to {@code movie(id)}).
     */
    @Id
    @Column(name = "movie_id")
    private Long movieId;

    /** Verified MIME type detected from image bytes (e.g., {@code image/jpeg}, {@code image/png}, {@code image/webp}). */
    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    /** Raw image bytes mapped to PostgreSQL's {@code BYTEA} binary column type. */
    @Column(name = "data", nullable = false, columnDefinition = "bytea")
    private byte[] data;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MovieImage() {
    }

    public MovieImage(Long movieId, String contentType, byte[] data) {
        this.movieId = movieId;
        this.contentType = contentType;
        this.data = data;
    }

    /**
     * JPA lifecycle callback invoked automatically right before inserting ({@link PrePersist})
     * or updating ({@link PreUpdate}) a poster row, keeping {@code updated_at} current.
     */
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}