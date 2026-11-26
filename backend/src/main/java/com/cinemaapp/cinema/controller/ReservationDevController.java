package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.ReservationRequest;
import com.cinemaapp.cinema.dto.ReservationResultResponse;
import com.cinemaapp.cinema.service.ReservationService;

import jakarta.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development-only endpoint for testing reservation processing
 * before the Kafka consumer is implemented.
 *
 * This controller is disabled unless the "dev" profile is active.
 * Spring Security still requires an authenticated cinema manager.
 *
 * Customers will NOT call this endpoint in the final architecture.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/dev/reservations")
public class ReservationDevController {

    private final ReservationService reservationService;

    public ReservationDevController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResultResponse> processReservation(
            @Valid @RequestBody ReservationRequest request
    ) {

        ReservationResultResponse response =
                reservationService.processReservation(request);

        return ResponseEntity.ok(response);
    }
}