package com.cinemaapp.cinema.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes complete showtime catalog snapshots to Kafka.
 *
 * Normal topic:
 * cinema.catalog
 *
 * Message key:
 * showtimeId
 *
 * Message value:
 * JSON snapshot matching the approved project contract.
 *
 * This producer does not decide which fields belong in
 * the catalog. It publishes the supplied snapshot without
 * renaming or adding JSON fields.
 *
 * A separate catalog service will later build snapshots
 * using the authoritative Cinema database.
 */
@Component
public class CatalogKafkaProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(CatalogKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final JsonMapper jsonMapper;

    private final String catalogTopic;

    /**
     * Spring injects:
     *
     * 1. KafkaTemplate for publishing messages.
     * 2. JsonMapper for JSON serialization.
     * 3. The topic name from application.properties.
     */
    public CatalogKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            JsonMapper jsonMapper,
            @Value("${app.kafka.catalog.topic:cinema.catalog}")
            String catalogTopic
    ) {

        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.catalogTopic = catalogTopic;
    }

    /**
     * Publishes one complete catalog snapshot.
     *
     * The snapshot must contain a numeric showtimeId.
     *
     * @param snapshot JSON-compatible catalog fields.
     * @return Published showtime ID.
     */
    public long publishSnapshot(Map<String, Object> snapshot) {

        // Step 1: Ensure the snapshot exists.
        if (snapshot == null) {

            throw new IllegalArgumentException(
                    "Catalog snapshot is required"
            );
        }

        // Step 2: Read the showtime ID from the snapshot.
        Object rawShowtimeId = snapshot.get("showtimeId");

        // A JSON integer is normally mapped to Integer or Long.
        // Do not allow a missing, fractional, or string ID.
        if (!(rawShowtimeId instanceof Integer)
                && !(rawShowtimeId instanceof Long)) {

            throw new IllegalArgumentException(
                    "Catalog snapshot must contain a numeric showtimeId"
            );
        }

        long showtimeId = ((Number) rawShowtimeId).longValue();

        if (showtimeId <= 0) {

            throw new IllegalArgumentException(
                    "Showtime ID must be positive"
            );
        }

        // Step 3: Use the ID as the Kafka message key.
        String messageKey = String.valueOf(showtimeId);

        // Step 4: Serialize the supplied snapshot to JSON.
        //
        // Map keys are preserved, so we do not rename fields
        // from the approved Kafka contract.
        String jsonMessage =
                jsonMapper.writeValueAsString(snapshot);

        try {

            // Step 5: Publish the JSON to the configured topic.
            //
            // Wait for Kafka to acknowledge the message.
            SendResult<String, String> sendResult =
                    kafkaTemplate.send(
                            catalogTopic,
                            messageKey,
                            jsonMessage
                    ).get(15, TimeUnit.SECONDS);

            // Step 6: Log successful publication.
            logger.info(
                    "Catalog snapshot published: showtimeId={}, topic={}, partition={}, offset={}",
                    showtimeId,
                    catalogTopic,
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset()
            );

            return showtimeId;

        } catch (InterruptedException exception) {

            // Restore the interrupted status.
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while publishing catalog snapshot",
                    exception
            );

        } catch (ExecutionException | TimeoutException exception) {

            throw new IllegalStateException(
                    "Could not confirm catalog snapshot publication",
                    exception
            );
        }
    }

    /**
     * Returns the currently configured Kafka topic.
     */
    public String getCatalogTopic() {
        return catalogTopic;
    }
}