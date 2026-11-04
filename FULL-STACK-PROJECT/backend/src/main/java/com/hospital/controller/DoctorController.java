package com.hospital.controller;

import com.hospital.dto.Dto.DoctorResponse;
import com.hospital.dto.Dto.SlotResponse;
import com.hospital.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Browsing doctors. Any logged-in user can use these.
 *
 *   GET /api/doctors                       - all doctors
 *   GET /api/doctors?specialization=card   - search by specialization
 *   GET /api/doctors/1                     - one doctor
 *   GET /api/doctors/1/slots               - free slots of doctor 1
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorResponse> search(@RequestParam(required = false) String specialization) {
        return doctorService.searchDoctors(specialization);
    }

    @GetMapping("/{id}")
    public DoctorResponse getOne(@PathVariable Long id) {
        return doctorService.getDoctor(id);
    }

    @GetMapping("/{id}/slots")
    public List<SlotResponse> getSlots(@PathVariable Long id) {
        return doctorService.getAvailableSlots(id);
    }
}
