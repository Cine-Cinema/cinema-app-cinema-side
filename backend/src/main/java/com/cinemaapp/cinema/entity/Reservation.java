package com.cinemaapp.cinema.entity;

import com.cinemaapp.cinema.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Represents a customer reservation for a particular showtime.
 *
 * One reservation can contain multiple reserved seats.
 * The seats are stored separately in the reservation_seat table.
 *
 * The reservation ID originates from the Fan backend and is reused
 * when processing Kafka messages to help prevent duplicate bookings.
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    /**
     * Unique reservation identifier supplied by the Fan backend.
     *
     * Example: 550e8400-e29b-41d4-a716-446655440000
     */
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /**
     * Identifies the screening associated with this reservation.
     */
    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    /**
     * Customer identifier from the Fan application.
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * Customer name received from the Fan application.
     */
    @Column(name = "customer_name", length = 100)
    private String customerName;

    /**
     * Reservation outcome stored as a readable string in PostgreSQL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    /**
     * Explanation for a rejected reservation.
     * Usually null when a reservation is confirmed.
     */
    @Column(name = "reason", length = 200)
    private String reason;

    /**
     * Total reservation amount.
     *
     * BigDecimal is used instead of double because prices
     * require accurate decimal calculations.
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Date and time when this reservation was first stored.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Required by JPA when loading entities from PostgreSQL.
     */
    public Reservation() {
    }

    /**
     * Creates a reservation with the information received
     * from a processed booking request.
     */
    public Reservation(
            String id,
            Long showtimeId,
            Long customerId,
            String customerName,
            ReservationStatus status,
            String reason,
            BigDecimal totalAmount
    ) {
        this.id = id;
        this.showtimeId = showtimeId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.status = status;
        this.reason = reason;
        this.totalAmount = totalAmount;
    }

    /**
     * Sets the creation time before the first database insert.
     */
    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now(
                    ZoneId.of("Asia/Colombo")
            );
        }
    }

    public String getId() {
        return id;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}