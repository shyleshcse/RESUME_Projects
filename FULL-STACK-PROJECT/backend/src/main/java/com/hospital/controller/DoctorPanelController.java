package com.hospital.controller;

import com.hospital.dto.Dto.AddSlotRequest;
import com.hospital.dto.Dto.AppointmentResponse;
import com.hospital.dto.Dto.SlotResponse;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DOCTOR endpoints (the AuthInterceptor already checked the role is DOCTOR).
 *
 *   POST /api/doctor/slots                     - add a new time slot
 *   GET  /api/doctor/slots                     - see all my slots
 *   GET  /api/doctor/appointments              - see patients who booked me
 *   PUT  /api/doctor/appointments/7/complete   - mark appointment 7 as done
 */
@RestController
@RequestMapping("/api/doctor")
public class DoctorPanelController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorPanelController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/slots")
    public SlotResponse addSlot(@RequestBody AddSlotRequest body, HttpServletRequest request) {
        return doctorService.addSlot((Long) request.getAttribute("userId"), body);
    }

    @GetMapping("/slots")
    public List<SlotResponse> mySlots(HttpServletRequest request) {
        return doctorService.getMySlots((Long) request.getAttribute("userId"));
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> myAppointments(HttpServletRequest request) {
        return appointmentService.getDoctorAppointments((Long) request.getAttribute("userId"));
    }

    @PutMapping("/appointments/{id}/complete")
    public AppointmentResponse complete(@PathVariable Long id, HttpServletRequest request) {
        return appointmentService.complete((Long) request.getAttribute("userId"), id);
    }
}
