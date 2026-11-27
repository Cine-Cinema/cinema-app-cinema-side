package com.cinemaapp.cinema.kafka;

import com.cinemaapp.cinema.dto.ReservationResultResponse;
import com.cinemaapp.cinema.enums.ReservationStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes reservation results from Cinema Backend to Fan Backend.
 *
 * Kafka topic:
 * reservation.results
 *
 * Message key:
 * reservationId
 *
 * Message value:
 * JSON containing reservationId, status, and reason.
 *
 * This class does not access PostgreSQL.
 * It only publishes the result supplied by the caller.
 */
@Component
public class ReservationResultProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(ReservationResultProducer.class);

    // Use the topic name agreed in our shared contract.
    private static final String RESULT_TOPIC =
            "reservation.results";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    /**
     * Spring injects KafkaTemplate and JsonMapper automatically.
     */
    public ReservationResultProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Publishes a reservation decision to Kafka.
     *
     * This method waits for a broker acknowledgment.
     * It throws an exception if publication cannot be confirmed.
     */
    public void publishResult(ReservationResultResponse result) {

        // The agreed initial booking result contract uses
        // CONFIRMED and REJECTED, not PAID.
        //
        // If an already-paid booking request is redelivered,
        // return its original booking decision as CONFIRMED.
        ReservationStatus outgoingStatus =
                result.getStatus() == ReservationStatus.PAID
                        ? ReservationStatus.CONFIRMED
                        : result.getStatus();

        // Preserve the exact agreed JSON response fields.
        ReservationResultResponse outgoingResult =
                new ReservationResultResponse(
                        result.getReservationId(),
                        outgoingStatus,
                        result.getReason()
                );

        // Convert the Java DTO into a JSON String.
        String jsonMessage =
                jsonMapper.writeValueAsString(outgoingResult);

        try {

            // Send to Kafka using reservationId as the key.
            //
            // get() waits for the send to complete or fail.
            SendResult<String, String> sendResult =
                    kafkaTemplate.send(
                            RESULT_TOPIC,
                            outgoingResult.getReservationId(),
                            jsonMessage
                    ).get(15, TimeUnit.SECONDS);

            // Log the broker location of the published record.
            logger.info(
                    "Reservation result published: id={}, status={}, topic={}, partition={}, offset={}",
                    outgoingResult.getReservationId(),
                    outgoingStatus,
                    RESULT_TOPIC,
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset()
            );

        } catch (InterruptedException exception) {

            // Restore the thread's interrupted state.
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while publishing reservation result",
                    exception
            );

        } catch (ExecutionException | TimeoutException exception) {

            // Do not pretend publication succeeded.
            throw new IllegalStateException(
                    "Could not confirm Kafka reservation result publication",
                    exception
            );
        }
    }
}