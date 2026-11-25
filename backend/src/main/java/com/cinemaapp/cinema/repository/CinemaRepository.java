package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Cinema} persistence operations.
 *
 * <p>Extending {@link JpaRepository} automatically provides standard CRUD methods
 * ({@code save}, {@code findById}, {@code count}, etc.) backed by PostgreSQL without
 * writing boilerplate SQL or DAO implementations.
 */
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    /**
     * Derived query method that looks up a cinema account by its normalized email address.
     *
     * @param email lowercase cinema email address
     * @return an {@link Optional} containing the matching {@link Cinema}, or empty if not found
     */
    Optional<Cinema> findByEmail(String email);

    /**
     * Derived query method that checks whether an account with the given email already exists.
     *
     * @param email lowercase cinema email address
     * @return {@code true} if a cinema record already uses this email
     */
    boolean existsByEmail(String email);
}