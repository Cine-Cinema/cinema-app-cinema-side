package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

}