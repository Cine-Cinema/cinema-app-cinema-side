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

@RestController
@RequestMapping("/api/cinema/profile")
public class CinemaProfileController {

    private final CinemaProfileService cinemaProfileService;

    public CinemaProfileController(
            CinemaProfileService cinemaProfileService
    ) {
        this.cinemaProfileService = cinemaProfileService;
    }

    // Get cinema profile
    @GetMapping
    public ResponseEntity<CinemaResponse> getProfile(
            @AuthenticationPrincipal Long cinemaId
    ) {

        CinemaResponse response =
                cinemaProfileService.getProfile(cinemaId);

        return ResponseEntity.ok(response);
    }

    // Update cinema profile
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