package com.cinemaapp.cinema.kafka;

import com.cinemaapp.cinema.service.CatalogSnapshotService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Publishes updated catalog snapshots after database commits.
 *
 * Workflow:
 *
 * Database transaction
 *         |
 *         v
 * CatalogRefreshRequestedEvent
 *         |
 *         v
 * Transaction COMMIT
 *         |
 *         v
 * CatalogAfterCommitListener
 *         |
 *         v
 * CatalogSnapshotService
 *         |
 *         v
 * CatalogKafkaProducer
 *
 * Publication failures are logged because the original
 * database transaction has already committed.
 */
@Component
public class CatalogAfterCommitListener {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CatalogAfterCommitListener.class
            );

    private final CatalogSnapshotService catalogSnapshotService;

    private final CatalogKafkaProducer catalogKafkaProducer;

    public CatalogAfterCommitListener(
            CatalogSnapshotService catalogSnapshotService,
            CatalogKafkaProducer catalogKafkaProducer
    ) {
        this.catalogSnapshotService = catalogSnapshotService;
        this.catalogKafkaProducer = catalogKafkaProducer;
    }

    /**
     * Handles the event only after a successful database commit.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onCatalogRefreshRequested(
            CatalogRefreshRequestedEvent event
    ) {

        try {

            // 1. Read the latest committed state.
            Map<String, Object> snapshot =
                    catalogSnapshotService.buildSnapshot(
                            event.showtimeId()
                    );

            // 2. Publish the complete catalog snapshot.
            catalogKafkaProducer.publishSnapshot(snapshot);

            // 3. Log success.
            logger.info(
                    "Automatic catalog refresh completed for showtimeId={}",
                    event.showtimeId()
            );

        } catch (Exception exception) {

            // The database transaction has already committed.
            //
            // Do not claim that the database operation rolled back.
            logger.error(
                    "Catalog refresh failed AFTER database commit. showtimeId={}",
                    event.showtimeId(),
                    exception
            );
        }
    }
}