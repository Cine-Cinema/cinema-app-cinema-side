package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    Optional<Cinema> findByEmail(String email);

    boolean existsByEmail(String email);
}