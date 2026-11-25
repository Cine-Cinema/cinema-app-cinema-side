package com.cinemaapp.cinema.enums;

/**
 * Represents the status of a reservation managed by the Cinema backend.
 *
 * CONFIRMED - The requested seats have been successfully reserved.
 * REJECTED  - The reservation request was not accepted.
 * PAID      - The customer has paid for a confirmed reservation.
 *
 * The Fan backend may additionally use PENDING while waiting for
 * the Cinema backend to process a Kafka reservation request.
 */
public enum ReservationStatus {

    CONFIRMED,
    REJECTED,
    PAID
}