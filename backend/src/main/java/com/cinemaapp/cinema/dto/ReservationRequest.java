package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Represents a reservation request received from the Fan backend.
 *
 * The Fan backend generates the reservation ID.
 * The Cinema backend validates the requested showtime and seats.
 *
 * Ticket prices are intentionally NOT accepted from the Fan backend.
 */
public class ReservationRequest {

    @NotBlank
    @Pattern(
            regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
            message = "Reservation ID must be a valid UUID string"
    )
    private String reservationId;

    @NotNull
    @Positive
    private Long showtimeId;

    @NotNull
    @Positive
    private Long customerId;

    @NotBlank
    @Size(max = 100)
    private String customerName;

    @NotEmpty
    private List<@NotNull @Positive Integer> seatNumbers;

    public ReservationRequest() {
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<Integer> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<Integer> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}