package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.MovieImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link MovieImage} binary poster storage.
 *
 * <p>Because {@link MovieImage} uses {@code movieId} ({@code Long}) as its primary key,
 * methods like {@code findById(movieId)} and {@code existsById(movieId)} look up posters
 * directly by movie ID.
 */
public interface MovieImageRepository
        extends JpaRepository<MovieImage, Long> {
}