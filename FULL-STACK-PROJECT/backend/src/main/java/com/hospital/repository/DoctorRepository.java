package com.hospital.repository;

import com.hospital.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Find the doctor profile that belongs to a login account
    Optional<Doctor> findByUserId(Long userId);

    // Search by specialization, ignoring capital/small letters. "card" finds "Cardiology"
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
}
