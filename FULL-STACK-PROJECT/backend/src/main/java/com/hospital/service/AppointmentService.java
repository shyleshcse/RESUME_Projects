package com.hospital.service;

import com.hospital.dto.Dto.AppointmentResponse;
import com.hospital.exception.ApiException;
import com.hospital.model.Appointment;
import com.hospital.model.Doctor;
import com.hospital.model.Slot;
import com.hospital.model.User;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.SlotRepository;
import com.hospital.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * The most important service: booking, cancelling and completing appointments.
 *
 * Status life of an appointment:
 *     BOOKED ---> CANCELLED   (patient cancels, slot becomes free again)
 *     BOOKED ---> COMPLETED   (doctor marks it done after the visit)
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, SlotRepository slotRepository,
                              UserRepository userRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    // ---------------- PATIENT ----------------

    /**
     * BOOK A SLOT - the heart of the project, including the double-booking protection.
     *
     * @Transactional keeps everything below in ONE database transaction, so the row lock
     * taken by findByIdForUpdate() is held until the method finishes.
     *
     * Steps:
     *   1. Lock the slot row  (a second patient clicking at the same time must WAIT here)
     *   2. If slot.booked is already true -> error 409 (this is the double-booking check)
     *   3. Set slot.booked = true
     *   4. Save a new Appointment with status BOOKED
     */
    @Transactional
    public AppointmentResponse book(Long patientId, Long slotId) {
        if (slotId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "slotId is required");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // Step 1: lock the slot
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Slot not found"));

        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This slot is in the past");
        }

        // Step 2: double-booking check
        if (slot.isBooked()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Sorry, this slot was just booked by someone else. Please pick another slot.");
        }

        // Step 3 + 4
        slot.setBooked(true);
        slotRepository.save(slot);
        Appointment appointment = appointmentRepository.save(new Appointment(patient, slot));

        return toResponse(appointment);
    }

    public List<AppointmentResponse> getMyAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByIdDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    /** Patient cancels his own appointment. The slot becomes free for other patients. */
    @Transactional
    public AppointmentResponse cancel(Long patientId, Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This is not your appointment");
        }
        if (!appointment.getStatus().equals("BOOKED")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only BOOKED appointments can be cancelled (this one is " + appointment.getStatus() + ")");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);

        // Free the slot again
        Slot slot = slotRepository.findByIdForUpdate(appointment.getSlot().getId()).orElseThrow();
        slot.setBooked(false);
        slotRepository.save(slot);

        return toResponse(appointment);
    }

    // ---------------- DOCTOR ----------------

    public List<AppointmentResponse> getDoctorAppointments(Long doctorUserId) {
        Doctor doctor = findDoctorByUserId(doctorUserId);
        return appointmentRepository.findAllOfDoctor(doctor.getId())
                .stream().map(this::toResponse).toList();
    }

    /** Doctor marks a BOOKED appointment as COMPLETED after seeing the patient. */
    public AppointmentResponse complete(Long doctorUserId, Long appointmentId) {
        Doctor doctor = findDoctorByUserId(doctorUserId);
        Appointment appointment = findAppointment(appointmentId);

        if (!appointment.getSlot().getDoctor().getId().equals(doctor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This appointment is not with you");
        }
        if (!appointment.getStatus().equals("BOOKED")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only BOOKED appointments can be completed (this one is " + appointment.getStatus() + ")");
        }

        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    // ---------------- ADMIN ----------------

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByIdDesc()
                .stream().map(this::toResponse).toList();
    }

    // ---------------- small helpers ----------------

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private Doctor findDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    private AppointmentResponse toResponse(Appointment a) {
        Slot s = a.getSlot();
        return new AppointmentResponse(a.getId(), a.getPatient().getName(),
                s.getDoctor().getUser().getName(), s.getDoctor().getSpecialization(),
                s.getSlotDate(), s.getStartTime(), s.getEndTime(), a.getStatus());
    }
}
