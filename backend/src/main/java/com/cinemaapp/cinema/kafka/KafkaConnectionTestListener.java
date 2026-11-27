package com.cinemaapp.cinema.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Development-only Kafka listener.
 *
 * This class verifies that the Cinema Spring Boot backend
 * can receive messages from the shared Kafka broker.
 *
 * It listens only to the temporary cine.connection-test topic.
 *
 * It does not process reservations or modify the database.
 */
@Component
@Profile("dev")
public class KafkaConnectionTestListener {

    private static final Logger logger =
            LoggerFactory.getLogger(KafkaConnectionTestListener.class);

    /**
     * Automatically executes when a Kafka test message arrives.
     *
     * @param message The text received from Kafka.
     */
    @KafkaListener(
            topics = "cine.connection-test",
            groupId = "cinema-dev-connection-test-v1"
    )
    public void receiveMessage(String message) {

        logger.info(
                "KAFKA TEST MESSAGE RECEIVED: {}",
                message
        );
    }
}