package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.kafka.CatalogKafkaProducer;
import com.cinemaapp.cinema.service.CatalogSnapshotService;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Development-only controller for testing Kafka catalog publishing.
 *
 * Available only when Spring's dev profile is active.
 *
 * Both endpoints require Cinema Manager authentication.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/dev/catalog")
public class CatalogDevController {

    private final CatalogKafkaProducer catalogKafkaProducer;
    private final CatalogSnapshotService catalogSnapshotService;

    public CatalogDevController(
            CatalogKafkaProducer catalogKafkaProducer,
            CatalogSnapshotService catalogSnapshotService
    ) {
        this.catalogKafkaProducer = catalogKafkaProducer;
        this.catalogSnapshotService = catalogSnapshotService;
    }

    /**
     * Original Lesson 1.69 endpoint.
     *
     * Publishes manually supplied catalog JSON.
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publish(
            @RequestBody Map<String, Object> snapshot
    ) {

        try {

            long showtimeId =
                    catalogKafkaProducer.publishSnapshot(snapshot);

            return ResponseEntity.ok(
                    Map.of(
                            "result", "PUBLISHED",
                            "showtimeId", showtimeId,
                            "topic", catalogKafkaProducer.getCatalogTopic()
                    )
            );

        } catch (IllegalArgumentException exception) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
            );
        }
    }

    /**
     * NEW endpoint.
     *
     * Builds a snapshot from the real Cinema database
     * and then publishes it through Kafka.
     *
     * POST /api/dev/catalog/publish-live/{showtimeId}
     */
    @PostMapping("/publish-live/{showtimeId}")
    public ResponseEntity<Map<String, Object>> publishLive(
            @PathVariable Long showtimeId
    ) {

        // 1. Build the snapshot from PostgreSQL.
        Map<String, Object> snapshot =
                catalogSnapshotService.buildSnapshot(showtimeId);

        // 2. Publish the complete snapshot through Kafka.
        catalogKafkaProducer.publishSnapshot(snapshot);

        // 3. Return a confirmation.
        return ResponseEntity.ok(
                Map.of(
                        "result", "PUBLISHED",
                        "showtimeId", showtimeId,
                        "topic", catalogKafkaProducer.getCatalogTopic()
                )
        );
    }
}