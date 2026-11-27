package com.cinemaapp.cinema.grpc;

import com.cinemaapp.cinema.entity.Showtime;

import com.cinemaapp.cinema.grpc.generated.CatalogServiceGrpc;
import com.cinemaapp.cinema.grpc.generated.ListShowtimesRequest;
import com.cinemaapp.cinema.grpc.generated.ShowtimeSnapshot;

import com.cinemaapp.cinema.repository.ShowtimeRepository;

import com.cinemaapp.cinema.service.CatalogSnapshotService;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Cinema gRPC server.
 *
 * Provides complete catalog synchronization
 * for the Fan backend.
 *
 * This service is read-only.
 *
 * It does not modify PostgreSQL or publish Kafka messages.
 */
@GrpcService
public class CatalogGrpcService
        extends CatalogServiceGrpc.CatalogServiceImplBase {

    private static final Logger logger =
            LoggerFactory.getLogger(CatalogGrpcService.class);

    private final ShowtimeRepository showtimeRepository;

    private final CatalogSnapshotService catalogSnapshotService;

    /**
     * Constructor dependency injection.
     */
    public CatalogGrpcService(
            ShowtimeRepository showtimeRepository,
            CatalogSnapshotService catalogSnapshotService
    ) {
        this.showtimeRepository = showtimeRepository;
        this.catalogSnapshotService = catalogSnapshotService;
    }

    /**
     * Streams every current showtime to the Fan backend.
     *
     * One request produces zero or more ShowtimeSnapshot
     * responses, followed by stream completion.
     */
    @Override
    public void listShowtimes(
            ListShowtimesRequest request,
            StreamObserver<ShowtimeSnapshot> responseObserver
    ) {

        try {

            // STEP 1:
            // Get all showtime IDs from PostgreSQL.
            List<Long> showtimeIds = showtimeRepository
                    .findAll()
                    .stream()
                    .map(Showtime::getId)
                    .sorted()
                    .toList();

            logger.info(
                    "gRPC ListShowtimes requested. Total showtimes={}",
                    showtimeIds.size()
            );

            // STEP 2:
            // Process every showtime.
            for (Long showtimeId : showtimeIds) {

                // STEP 3:
                // Build the existing 14-field catalog snapshot.
                Map<String, Object> snapshot =
                        catalogSnapshotService.buildSnapshot(
                                showtimeId
                        );

                // STEP 4:
                // Convert the snapshot into a Protobuf message.
                ShowtimeSnapshot grpcMessage =
                        convertToGrpcMessage(snapshot);

                // STEP 5:
                // Send one showtime to the Fan client.
                responseObserver.onNext(grpcMessage);

                logger.debug(
                        "gRPC snapshot sent. showtimeId={}",
                        showtimeId
                );
            }

            // STEP 6:
            // Tell the client the stream completed successfully.
            responseObserver.onCompleted();

            logger.info(
                    "gRPC ListShowtimes completed. Total sent={}",
                    showtimeIds.size()
            );

        } catch (Exception exception) {

            logger.error(
                    "Failed to stream catalog using gRPC",
                    exception
            );

            // Report failure instead of pretending
            // that the complete catalog was delivered.
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(
                                    "Unable to stream cinema catalog"
                            )
                            .withCause(exception)
                            .asRuntimeException()
            );
        }
    }

    /**
     * Converts the existing Kafka-compatible catalog snapshot
     * into the proposed Protobuf ShowtimeSnapshot message.
     *
     * The input map comes from CatalogSnapshotService.
     */
    private ShowtimeSnapshot convertToGrpcMessage(
            Map<String, Object> snapshot
    ) {

        ShowtimeSnapshot.Builder builder =
                ShowtimeSnapshot.newBuilder()

                        .setCinemaName(
                                (String) snapshot.get("cinemaName")
                        )

                        .setMovieId(
                                ((Number) snapshot.get("movieId"))
                                        .longValue()
                        )

                        .setTitle(
                                (String) snapshot.get("title")
                        )

                        .setShowtimeId(
                                ((Number) snapshot.get("showtimeId"))
                                        .longValue()
                        )

                        .setShowDate(
                                (String) snapshot.get("showDate")
                        )

                        .setShowTime(
                                (String) snapshot.get("showTime")
                        )

                        .setPrice(
                                new BigDecimal(
                                        snapshot.get("price").toString()
                                ).toPlainString()
                        )

                        .setTotalSeats(
                                ((Number) snapshot.get("totalSeats"))
                                        .intValue()
                        )

                        .setSeatsPerRow(
                                ((Number) snapshot.get("seatsPerRow"))
                                        .intValue()
                        )

                        .setAvailableSeats(
                                ((Number) snapshot.get("availableSeats"))
                                        .intValue()
                        )

                        .setActive(
                                (Boolean) snapshot.get("active")
                        );

        // The description may be null.
        Object description = snapshot.get("description");

        if (description != null) {
            builder.setDescription(description.toString());
        }

        // The poster URL may be null.
        Object posterUrl = snapshot.get("posterUrl");

        if (posterUrl != null) {
            builder.setPosterUrl(posterUrl.toString());
        }

        // Convert the reserved seat list.
        List<?> reservedSeats =
                (List<?>) snapshot.get("reservedSeats");

        for (Object seat : reservedSeats) {

            builder.addReservedSeats(
                    ((Number) seat).intValue()
            );
        }

        // Build the immutable Protobuf message.
        return builder.build();
    }
}