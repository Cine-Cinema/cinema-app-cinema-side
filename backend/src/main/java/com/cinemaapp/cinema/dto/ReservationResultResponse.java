package com.cinemaapp.cinema.dto;

import com.cinemaapp.cinema.enums.ReservationStatus;

/**
 * Describes the result of processing a reservation request.
 *
 * This response can later be mapped to the Kafka
 * reservation.results message contract.
 */
public class ReservationResultResponse {

    private final String reservationId;
    private final ReservationStatus status;
    private final String reason;

    public ReservationResultResponse(
            String reservationId,
            ReservationStatus status,
            String reason
    ) {
        this.reservationId = reservationId;
        this.status = status;
        this.reason = reason;
    }

    public String getReservationId() {
        return reservationId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }
}