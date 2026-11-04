package com.hospital.controller;

import com.hospital.dto.Dto.AddDoctorRequest;
import com.hospital.dto.Dto.AppointmentResponse;
import com.hospital.dto.Dto.DoctorResponse;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADMIN endpoints (the AuthInterceptor already checked the role is ADMIN).
 *
 *   POST /api/admin/doctors        - create a doctor account + profile
 *   GET  /api/admin/appointments   - see every appointment in the hospital
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public AdminController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/doctors")
    public DoctorResponse addDoctor(@RequestBody AddDoctorRequest body) {
        return doctorService.addDoctor(body);
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> allAppointments() {
        return appointmentService.getAllAppointments();
    }
}
