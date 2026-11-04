package com.hospital.controller;

import com.hospital.dto.Dto.AppointmentResponse;
import com.hospital.dto.Dto.BookRequest;
import com.hospital.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PATIENT endpoints (the AuthInterceptor already checked the role is PATIENT).
 *
 *   POST /api/appointments            body: { "slotId": 5 }   - book a slot
 *   GET  /api/appointments/my                                  - my appointments
 *   PUT  /api/appointments/7/cancel                            - cancel appointment 7
 *
 * "userId" was put on the request by AuthInterceptor after reading the JWT token.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public AppointmentResponse book(@RequestBody BookRequest body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return appointmentService.book(userId, body.slotId());
    }

    @GetMapping("/my")
    public List<AppointmentResponse> myAppointments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return appointmentService.getMyAppointments(userId);
    }

    @PutMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return appointmentService.cancel(userId, id);
    }
}
