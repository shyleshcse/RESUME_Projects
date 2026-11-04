package com.hospital.repository;

import com.hospital.model.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    // Free slots of a doctor from today onwards, earliest first
    @Query("select s from Slot s where s.doctor.id = :doctorId and s.booked = false "
         + "and s.slotDate >= :today order by s.slotDate, s.startTime")
    List<Slot> findAvailable(@Param("doctorId") Long doctorId, @Param("today") LocalDate today);

    // All slots of a doctor (free + booked) - for the doctor's own dashboard
    @Query("select s from Slot s where s.doctor.id = :doctorId order by s.slotDate, s.startTime")
    List<Slot> findAllOfDoctor(@Param("doctorId") Long doctorId);

    // Stops a doctor from adding the same time twice
    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);

    /**
     * THE KEY TO PREVENTING DOUBLE BOOKING.
     * PESSIMISTIC_WRITE = "SELECT ... FOR UPDATE" in SQL.
     * It LOCKS this row. If two patients click "Book" at the same moment, the database
     * makes the second one WAIT until the first one finishes. Then the second one sees
     * booked = true and gets an error. See docs/06-WORKFLOWS.md for a real-life example.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);
}
