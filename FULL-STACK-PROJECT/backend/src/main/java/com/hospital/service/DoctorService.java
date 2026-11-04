package com.hospital.service;

import com.hospital.dto.Dto.AddDoctorRequest;
import com.hospital.dto.Dto.AddSlotRequest;
import com.hospital.dto.Dto.DoctorResponse;
import com.hospital.dto.Dto.SlotResponse;
import com.hospital.exception.ApiException;
import com.hospital.model.Doctor;
import com.hospital.model.Slot;
import com.hospital.model.User;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.SlotRepository;
import com.hospital.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Everything about doctors and their slots:
 *   - admin adds a doctor
 *   - patients search doctors and see free slots
 *   - a doctor adds / views his own slots
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository,
                         SlotRepository slotRepository, PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------- ADMIN ----------------

    /**
     * Creates 2 rows: a User (the login) and a Doctor (the profile).
     * @Transactional = "do both or none". If the 2nd save fails, the 1st one is undone.
     */
    @Transactional
    public DoctorResponse addDoctor(AddDoctorRequest req) {
        if (isBlank(req.name()) || isBlank(req.email()) || isBlank(req.password())
                || isBlank(req.specialization())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Name, email, password and specialization are required");
        }
        if (req.password().length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
        }

        int experience = req.experienceYears() == null ? 0 : req.experienceYears();

        User user = userRepository.save(
                new User(req.name().trim(), email, passwordEncoder.encode(req.password()), "DOCTOR"));
        Doctor doctor = doctorRepository.save(new Doctor(user, req.specialization().trim(), experience));

        return toDoctorResponse(doctor);
    }

    // ---------------- ANY LOGGED-IN USER ----------------

    /** If "specialization" is empty we return all doctors, otherwise only matching ones. */
    public List<DoctorResponse> searchDoctors(String specialization) {
        List<Doctor> doctors;
        if (specialization == null || specialization.isBlank()) {
            doctors = doctorRepository.findAll();
        } else {
            doctors = doctorRepository.findBySpecializationContainingIgnoreCase(specialization.trim());
        }
        return doctors.stream().map(this::toDoctorResponse).toList();
    }

    public DoctorResponse getDoctor(Long doctorId) {
        return toDoctorResponse(findDoctor(doctorId));
    }

    /** Only FREE slots from today onwards. Booked slots are hidden from patients. */
    public List<SlotResponse> getAvailableSlots(Long doctorId) {
        findDoctor(doctorId); // gives a 404 error if the doctor does not exist
        return slotRepository.findAvailable(doctorId, LocalDate.now())
                .stream().map(this::toSlotResponse).toList();
    }

    // ---------------- DOCTOR ----------------

    public SlotResponse addSlot(Long userId, AddSlotRequest req) {
        Doctor doctor = findDoctorByUserId(userId);

        if (req.date() == null || req.startTime() == null || req.endTime() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date, start time and end time are required");
        }
        if (req.date().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot add a slot in the past");
        }
        if (!req.startTime().isBefore(req.endTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
        if (slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctor.getId(), req.date(), req.startTime())) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a slot at this date and time");
        }

        Slot slot = slotRepository.save(new Slot(doctor, req.date(), req.startTime(), req.endTime()));
        return toSlotResponse(slot);
    }

    public List<SlotResponse> getMySlots(Long userId) {
        Doctor doctor = findDoctorByUserId(userId);
        return slotRepository.findAllOfDoctor(doctor.getId())
                .stream().map(this::toSlotResponse).toList();
    }

    // ---------------- small helpers ----------------

    private Doctor findDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    private Doctor findDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    private DoctorResponse toDoctorResponse(Doctor d) {
        return new DoctorResponse(d.getId(), d.getUser().getName(), d.getUser().getEmail(),
                d.getSpecialization(), d.getExperienceYears());
    }

    private SlotResponse toSlotResponse(Slot s) {
        return new SlotResponse(s.getId(), s.getDoctor().getId(), s.getDoctor().getUser().getName(),
                s.getSlotDate(), s.getStartTime(), s.getEndTime(), s.isBooked());
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
