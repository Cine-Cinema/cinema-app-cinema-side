package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.CinemaResponse;
import com.cinemaapp.cinema.dto.UpdateCinemaProfileRequest;
import com.cinemaapp.cinema.service.CinemaProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Protected REST controller for reading and updating the logged-in cinema's profile.
 *
 * <p>Requires a valid JWT Bearer token. Instead of taking a cinema ID in the URL path,
 * both endpoints read {@code cinemaId} directly from Spring Security's
 * {@link AuthenticationPrincipal}, which {@code JwtAuthenticationFilter} populates from
 * the verified JWT subject so callers can only view or modify their own account.
 */
@RestController
@RequestMapping("/api/cinema/profile")
public class CinemaProfileController {

    private final CinemaProfileService cinemaProfileService;

    public CinemaProfileController(
            CinemaProfileService cinemaProfileService
    ) {
        this.cinemaProfileService = cinemaProfileService;
    }

    /**
     * Returns the authenticated cinema's profile details.
     *
     * @param cinemaId cinema account ID injected from the authenticated SecurityContext principal
     * @return HTTP 200 OK with {@link CinemaResponse}
     */
    @GetMapping
    public ResponseEntity<CinemaResponse> getProfile(
            @AuthenticationPrincipal Long cinemaId
    ) {

        CinemaResponse response =
                cinemaProfileService.getProfile(cinemaId);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the authenticated cinema's name, address, and email.
     *
     * @param cinemaId cinema account ID injected from the authenticated SecurityContext principal
     * @param request  validated profile update payload
     * @return HTTP 200 OK with the updated {@link CinemaResponse}
     */
    @PutMapping
    public ResponseEntity<CinemaResponse> updateProfile(
            @AuthenticationPrincipal Long cinemaId,
            @Valid @RequestBody UpdateCinemaProfileRequest request
    ) {

        CinemaResponse response =
                cinemaProfileService.updateProfile(cinemaId, request);

        return ResponseEntity.ok(response);
    }
}