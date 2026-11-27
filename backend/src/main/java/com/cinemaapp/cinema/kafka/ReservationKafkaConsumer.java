package com.cinemaapp.cinema.kafka;

import com.cinemaapp.cinema.dto.ReservationRequest;
import com.cinemaapp.cinema.dto.ReservationResultResponse;
import com.cinemaapp.cinema.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Receives reservation requests from Fan Backend through Kafka.
 *
 * Topic:
 * reservation.requests
 *
 * Workflow:
 * 1. Receive JSON request.
 * 2. Convert JSON into ReservationRequest.
 * 3. Process reservation using ReservationService.
 * 4. Publish the result to reservation.results.
 *
 * Database business logic remains inside ReservationService.
 */
@Component
public class ReservationKafkaConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    ReservationKafkaConsumer.class
            );

    private final ReservationService reservationService;

    private final ReservationResultProducer resultProducer;

    private final JsonMapper jsonMapper;

    /**
     * Constructor dependency injection.
     */
    public ReservationKafkaConsumer(
            ReservationService reservationService,
            ReservationResultProducer resultProducer,
            JsonMapper jsonMapper
    ) {
        this.reservationService = reservationService;
        this.resultProducer = resultProducer;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Processes one Kafka reservation request.
     *
     * Disabled by default until explicitly enabled for testing.
     */
    @KafkaListener(
            topics = "reservation.requests",
            groupId = "${app.kafka.reservation.consumer-group:cinema-reservation-service}",
            autoStartup = "${app.kafka.reservation.consumer-enabled:false}"
    )
    public void receiveReservation(String message) {

        logger.info("Received a Kafka reservation request");

        // STEP 1: Convert JSON into Java DTO.
        ReservationRequest request;

        try {

            request = jsonMapper.readValue(
                    message,
                    ReservationRequest.class
            );

        } catch (JacksonException exception) {

            logger.error(
                    "Invalid reservation JSON received from Kafka",
                    exception
            );

            // Tell the Kafka listener infrastructure that processing failed.
            throw new IllegalArgumentException(
                    "Cannot deserialize reservation request",
                    exception
            );
        }

        // STEP 2: Process the reservation in PostgreSQL.
        //
        // ReservationService uses @Transactional.
        // When its proxied method returns successfully,
        // its database transaction has committed.
        ReservationResultResponse result =
                reservationService.processReservation(request);

        logger.info(
                "Reservation database processing completed: id={}, status={}",
                result.getReservationId(),
                result.getStatus()
        );

        // STEP 3: Publish the decision to Kafka.
        //
        // This runs AFTER ReservationService returns.
        resultProducer.publishResult(result);

        // STEP 4: The result was acknowledged by Kafka.
        logger.info(
                "Reservation workflow completed: id={}",
                result.getReservationId()
        );
    }
}