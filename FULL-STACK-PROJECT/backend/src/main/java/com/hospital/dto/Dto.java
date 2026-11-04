package com.hospital.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO = Data Transfer Object = a simple box of data that travels between React and Spring Boot.
 *
 *  - "Request" boxes = JSON that React SENDS to us.
 *  - "Response" boxes = JSON that we SEND BACK to React.
 *
 * We keep them all in this one file so you can see every JSON shape of the project in one place.
 * A Java "record" is just a short way to write a class that only holds data.
 */
public class Dto {

    // ---------- Requests (React -> Backend) ----------
    public record RegisterRequest(String name, String email, String password) {}

    public record LoginRequest(String email, String password) {}

    public record AddDoctorRequest(String name, String email, String password,
                                   String specialization, Integer experienceYears) {}

    public record AddSlotRequest(LocalDate date, LocalTime startTime, LocalTime endTime) {}

    public record BookRequest(Long slotId) {}

    // ---------- Responses (Backend -> React) ----------
    public record AuthResponse(String token, Long userId, String name, String email, String role) {}

    public record DoctorResponse(Long id, String name, String email,
                                 String specialization, int experienceYears) {}

    public record SlotResponse(Long id, Long doctorId, String doctorName,
                               LocalDate date, LocalTime startTime, LocalTime endTime,
                               boolean booked) {}

    public record AppointmentResponse(Long id, String patientName, String doctorName,
                                      String specialization, LocalDate date,
                                      LocalTime startTime, LocalTime endTime, String status) {}
}
