package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.ReservationRequest;
import com.cinemaapp.cinema.dto.ReservationResultResponse;

import com.cinemaapp.cinema.entity.Reservation;
import com.cinemaapp.cinema.entity.ReservationSeat;
import com.cinemaapp.cinema.entity.Showtime;

import com.cinemaapp.cinema.enums.ReservationStatus;

import com.cinemaapp.cinema.exception.ResourceNotFoundException;

import com.cinemaapp.cinema.repository.ReservationRepository;
import com.cinemaapp.cinema.repository.ReservationSeatRepository;
import com.cinemaapp.cinema.repository.ShowtimeRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Validates and processes customer reservation requests.
 *
 * The Cinema backend is responsible for deciding whether
 * requested seats can be reserved.
 *
 * All database writes for one request occur in one transaction.
 * A pessimistic lock on the showtime coordinates concurrent bookings.
 */
@Service
public class ReservationService {

    private static final ZoneId CINEMA_TIME_ZONE =
            ZoneId.of("Asia/Colombo");

    private static final BigDecimal MAX_TOTAL_AMOUNT =
            new BigDecimal("99999999.99");

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final Validator validator;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationSeatRepository reservationSeatRepository,
            ShowtimeRepository showtimeRepository,
            Validator validator
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.showtimeRepository = showtimeRepository;
        this.validator = validator;
    }

    /**
     * Processes one reservation request.
     *
     * Valid requests receive a CONFIRMED or REJECTED result.
     * Structurally invalid requests receive HTTP 400 when called
     * through the development REST endpoint.
     *
     * The same service can later be called from a Kafka consumer.
     */
    @Transactional
    public ReservationResultResponse processReservation(
            ReservationRequest request
    ) {

        // 1. Validate the request structure.
        validateRequest(request);

        // 2. Handle an already-processed reservation ID.
        Optional<Reservation> existingReservation =
                reservationRepository.findById(
                        request.getReservationId()
                );

        if (existingReservation.isPresent()) {
            return resultFromExisting(
                    existingReservation.get(),
                    request
            );
        }

        // 3. Lock the selected showtime.
        Showtime showtime = showtimeRepository
                .findByIdForUpdate(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Showtime not found with id: "
                                + request.getShowtimeId()
                ));

        // 4. Check again after obtaining the lock.
        existingReservation = reservationRepository.findById(
                request.getReservationId()
        );

        if (existingReservation.isPresent()) {
            return resultFromExisting(
                    existingReservation.get(),
                    request
            );
        }

        // 5. Check whether bookings are enabled.
        if (!showtime.isActive()) {
            return rejectAndSave(
                    request,
                    "Showtime is not accepting reservations"
            );
        }

        // 6. Reject screenings that have already started.
        LocalDateTime showtimeStart = LocalDateTime.of(
                showtime.getShowDate(),
                showtime.getShowTime()
        );

        if (!showtimeStart.isAfter(
                LocalDateTime.now(CINEMA_TIME_ZONE)
        )) {
            return rejectAndSave(
                    request,
                    "Showtime has already started"
            );
        }

        List<Integer> requestedSeats = request.getSeatNumbers();

        // 7. Reject duplicate seat numbers within one request.
        if (new HashSet<>(requestedSeats).size()
                != requestedSeats.size()) {

            return rejectAndSave(
                    request,
                    "Reservation contains duplicate seat numbers"
            );
        }

        // 8. Verify each requested seat is within capacity.
        for (Integer seatNumber : requestedSeats) {

            if (seatNumber > showtime.getTotalSeats()) {
                return rejectAndSave(
                        request,
                        "Invalid seat number: " + seatNumber
                );
            }
        }

        // 9. Retrieve seats already allocated for this showtime.
        Set<Integer> occupiedSeats = new HashSet<>(
                showtimeRepository.findReservedSeatNumbers(
                        showtime.getId()
                )
        );

        // 10. Reject the entire request if any seat is occupied.
        for (Integer seatNumber : requestedSeats) {

            if (occupiedSeats.contains(seatNumber)) {
                return rejectAndSave(
                        request,
                        "Seat " + seatNumber + " is already reserved"
                );
            }
        }

        // 11. Calculate the amount using the cinema's ticket price.
        BigDecimal totalAmount = showtime.getPrice()
                .multiply(BigDecimal.valueOf(requestedSeats.size()));

        // The database column is NUMERIC(10,2).
        if (totalAmount.compareTo(MAX_TOTAL_AMOUNT) > 0) {
            return rejectAndSave(
                    request,
                    "Reservation amount exceeds the allowed limit"
            );
        }

        // 12. Create the confirmed reservation.
        Reservation reservation = new Reservation(
                request.getReservationId(),
                showtime.getId(),
                request.getCustomerId(),
                request.getCustomerName().trim(),
                ReservationStatus.CONFIRMED,
                null,
                totalAmount
        );

        // 13. Save the parent reservation first.
        reservationRepository.saveAndFlush(reservation);

        // 14. Create one ReservationSeat for each requested seat.
        List<ReservationSeat> seatsToSave = new ArrayList<>();

        for (Integer seatNumber : requestedSeats) {

            seatsToSave.add(new ReservationSeat(
                    reservation.getId(),
                    showtime.getId(),
                    seatNumber
            ));
        }

        // 15. Save all selected seats in the same transaction.
        reservationSeatRepository.saveAllAndFlush(seatsToSave);

        // 16. Return the confirmed result.
        return new ReservationResultResponse(
                reservation.getId(),
                ReservationStatus.CONFIRMED,
                null
        );
    }

    /**
     * Validates the request even when the service is called
     * directly by a future Kafka consumer.
     */
    private void validateRequest(ReservationRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation request is required"
            );
        }

        Set<ConstraintViolation<ReservationRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {

            ConstraintViolation<ReservationRequest> violation =
                    violations.iterator().next();

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid reservation request: "
                            + violation.getPropertyPath()
                            + " - "
                            + violation.getMessage()
            );
        }
    }

    /**
     * Returns an earlier result for a repeated reservation ID.
     *
     * For confirmed and paid reservations, the requested seat
     * numbers must match the seats originally saved.
     */
    private ReservationResultResponse resultFromExisting(
            Reservation existing,
            ReservationRequest request
    ) {

        boolean differentCustomerOrShowtime =
                !Objects.equals(
                        existing.getShowtimeId(),
                        request.getShowtimeId()
                )
                        || !Objects.equals(
                        existing.getCustomerId(),
                        request.getCustomerId()
                )
                        || !Objects.equals(
                        existing.getCustomerName(),
                        request.getCustomerName().trim()
                );

        if (differentCustomerOrShowtime) {
            return new ReservationResultResponse(
                    request.getReservationId(),
                    ReservationStatus.REJECTED,
                    "Reservation ID already belongs to a different request"
            );
        }

        if (existing.getStatus() != ReservationStatus.REJECTED) {

            List<Integer> originalSeats =
                    reservationSeatRepository
                            .findById_ReservationId(existing.getId())
                            .stream()
                            .map(ReservationSeat::getSeatNumber)
                            .sorted()
                            .toList();

            List<Integer> incomingSeats =
                    request.getSeatNumbers()
                            .stream()
                            .sorted()
                            .toList();

            if (!originalSeats.equals(incomingSeats)) {
                return new ReservationResultResponse(
                        request.getReservationId(),
                        ReservationStatus.REJECTED,
                        "Reservation ID was already used for different seats"
                );
            }
        }

        // Return the previously stored outcome.
        return new ReservationResultResponse(
                existing.getId(),
                existing.getStatus(),
                existing.getReason()
        );
    }

    /**
     * Saves a rejected reservation without allocating seats.
     *
     * The showtime must already exist because reservation.showtime_id
     * has a foreign-key constraint in PostgreSQL.
     */
    private ReservationResultResponse rejectAndSave(
            ReservationRequest request,
            String reason
    ) {

        Reservation rejectedReservation = new Reservation(
                request.getReservationId(),
                request.getShowtimeId(),
                request.getCustomerId(),
                request.getCustomerName().trim(),
                ReservationStatus.REJECTED,
                reason,
                BigDecimal.ZERO
        );

        reservationRepository.saveAndFlush(rejectedReservation);

        return new ReservationResultResponse(
                request.getReservationId(),
                ReservationStatus.REJECTED,
                reason
        );
    }
}