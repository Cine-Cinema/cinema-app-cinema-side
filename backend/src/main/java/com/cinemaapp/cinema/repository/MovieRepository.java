package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link Movie} persistence operations.
 *
 * <p>Inherits standard CRUD and existence-check operations (such as {@code findAll},
 * {@code findById}, {@code existsById}, {@code save}, and {@code delete}) from
 * {@link JpaRepository}.
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {

}