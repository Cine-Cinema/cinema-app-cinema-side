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

/**
 * Service responsible for reading and updating the authenticated cinema's profile details.
 *
 * <p>Uses Spring's {@link Transactional} boundaries to separate read-only queries from
 * atomic profile updates.
 */
@Service
public class CinemaProfileService {

    private final CinemaRepository cinemaRepository;

    public CinemaProfileService(CinemaRepository cinemaRepository) {
        this.cinemaRepository = cinemaRepository;
    }

    /**
     * Retrieves the profile of the currently authenticated cinema.
     *
     * <p>{@code @Transactional(readOnly = true)} allows Hibernate and PostgreSQL to skip
     * dirty-checking overhead since no entity state is modified.
     *
     * @param cinemaId authenticated cinema ID extracted from the JWT
     * @return public cinema profile fields (without password)
     * @throws ResponseStatusException with HTTP 404 Not Found if the cinema ID does not exist
     */
    @Transactional(readOnly = true)
    public CinemaResponse getProfile(Long cinemaId) {

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cinema account not found"
                ));

        return toResponse(cinema);
    }

    /**
     * Updates the authenticated cinema's name, optional address, and email inside a single
     * database transaction.
     *
     * @param cinemaId authenticated cinema ID extracted from the JWT
     * @param request  validated profile update payload
     * @return updated {@link CinemaResponse}
     * @throws ResponseStatusException with HTTP 404 if the account is missing, or 409 Conflict
     *                                 if the new email belongs to a different account
     */
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

        // Allow keeping the same email on this account, but reject collisions with any other account
        cinemaRepository.findByEmail(email).ifPresent(existingCinema -> {
            if (!existingCinema.getId().equals(cinemaId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This email is already registered"
                );
            }
        });

        // Update profile fields; convert blank addresses to null so empty strings aren't stored
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

    /**
     * Maps a {@link Cinema} entity to {@link CinemaResponse}, ensuring the password hash
     * is never exposed over the REST API.
     */
    private CinemaResponse toResponse(Cinema cinema) {

        return new CinemaResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getEmail()
        );
    }
}