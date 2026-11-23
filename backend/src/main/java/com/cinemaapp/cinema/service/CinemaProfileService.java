package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.CinemaResponse;
import com.cinemaapp.cinema.dto.UpdateCinemaProfileRequest;
import com.cinemaapp.cinema.entity.Cinema;
import com.cinemaapp.cinema.repository.CinemaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class CinemaProfileService {

    private final CinemaRepository cinemaRepository;

    public CinemaProfileService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    // Retrieve cinema profile
    @Transactional(readOnly = true)
    public CinemaResponse getProfile(Long cinemaId) {

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cinema account not found"
                ));

        return toResponse(cinema);
    }

    // Update cinema profile
    @Transactional
    public CinemaResponse updateProfile(
            Long cinemaId,
            UpdateCinemaProfileRequest request
    ) {

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cinema account not found"
                ));

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Check whether another account already uses this email
        cinemaRepository.findByEmail(email).ifPresent(existingCinema -> {
            if (!existingCinema.getId().equals(cinemaId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This email is already registered"
                );
            }
        });

        // Update profile fields
        cinema.setName(request.getName().trim());

        cinema.setAddress(
                request.getAddress() == null ||
                        request.getAddress().isBlank()
                        ? null
                        : request.getAddress().trim()
        );

        cinema.setEmail(email);

        // Save updated cinema
        Cinema updatedCinema = cinemaRepository.save(cinema);

        return toResponse(updatedCinema);
    }

    // Convert Cinema entity into a safe response DTO
    private CinemaResponse toResponse(Cinema cinema) {

        return new CinemaResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getEmail()
        );
    }
}