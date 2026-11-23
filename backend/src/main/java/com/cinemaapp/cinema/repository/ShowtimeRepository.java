package com.cinemaapp.cinema.repository;

import com.cinemaapp.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeRepository
        extends JpaRepository<Showtime, Long> {

    List<Showtime> findByShowDateOrderByShowTimeAsc(
            LocalDate showDate
    );

    List<Showtime> findByMovieIdOrderByShowDateAscShowTimeAsc(
            Long movieId
    );
}