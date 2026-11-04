package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One row in the "appointments" table = "this patient booked this slot".
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The patient who booked (a User with role PATIENT)
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;

    // The slot that was booked
    @ManyToOne
    @JoinColumn(name = "slot_id")
    private Slot slot;

    // "BOOKED", "CANCELLED" or "COMPLETED"
    private String status;

    private LocalDateTime createdAt;

    public Appointment() {}

    public Appointment(User patient, Slot slot) {
        this.patient = patient;
        this.slot = slot;
        this.status = "BOOKED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getPatient() { return patient; }
    public Slot getSlot() { return slot; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
