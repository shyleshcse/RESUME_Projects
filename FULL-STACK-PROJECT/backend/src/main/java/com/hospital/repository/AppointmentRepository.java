package com.hospital.repository;

import com.hospital.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // All appointments of one patient, newest first
    List<Appointment> findByPatientIdOrderByIdDesc(Long patientId);

    // All appointments of one doctor, by date and time
    @Query("select a from Appointment a where a.slot.doctor.id = :doctorId "
         + "order by a.slot.slotDate, a.slot.startTime")
    List<Appointment> findAllOfDoctor(@Param("doctorId") Long doctorId);

    // Every appointment in the hospital (for admin)
    List<Appointment> findAllByOrderByIdDesc();
}
