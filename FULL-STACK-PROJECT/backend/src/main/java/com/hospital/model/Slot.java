package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One row in the "slots" table = one bookable time of one doctor.
 * Example: Dr. Priya, 2026-11-04, 10:00 - 10:30, booked = false
 */
@Entity
@Table(name = "slots")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many slots belong to one doctor
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // false = free, true = someone already booked it
    private boolean booked;

    public Slot() {}

    public Slot(Doctor doctor, LocalDate slotDate, LocalTime startTime, LocalTime endTime) {
        this.doctor = doctor;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.booked = false;
    }

    public Long getId() { return id; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getSlotDate() { return slotDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }
}
