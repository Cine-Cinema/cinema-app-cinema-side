package com.cinemaapp.cinema.kafka;

/**
 * Internal Spring application event.
 *
 * Requests a catalog refresh for one showtime.
 *
 * This is NOT a Kafka message and is not part of
 * the shared Cinema/Fan JSON contract.
 *
 * It only tells our Cinema application which
 * showtime needs a new catalog snapshot.
 */
public record CatalogRefreshRequestedEvent(
        Long showtimeId
) {
}