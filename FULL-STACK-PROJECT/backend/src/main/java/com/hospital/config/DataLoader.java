package com.hospital.config;

import com.hospital.model.Doctor;
import com.hospital.model.Slot;
import com.hospital.model.User;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.SlotRepository;
import com.hospital.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Runs ONCE when the app starts. If the database is empty it adds demo data
 * so you can test immediately (1 admin, 1 patient, 3 doctors, and slots for the next 3 days).
 * If users already exist, it does nothing.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, DoctorRepository doctorRepository,
                      SlotRepository slotRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // data already there
        }

        userRepository.save(new User("Admin", "admin@hospital.com", passwordEncoder.encode("admin123"), "ADMIN"));
        userRepository.save(new User("Test Patient", "patient@test.com", passwordEncoder.encode("patient123"), "PATIENT"));

        createDoctor("Dr. Priya Sharma", "priya@hospital.com", "Cardiology", 12);
        createDoctor("Dr. Arun Kumar", "arun@hospital.com", "Dermatology", 8);
        createDoctor("Dr. Meena Iyer", "meena@hospital.com", "Pediatrics", 10);

        System.out.println();
        System.out.println("==================== DEMO LOGINS ====================");
        System.out.println("  ADMIN   : admin@hospital.com   / admin123");
        System.out.println("  DOCTOR  : priya@hospital.com   / doctor123");
        System.out.println("  PATIENT : patient@test.com     / patient123");
        System.out.println("=====================================================");
        System.out.println();
    }

    private void createDoctor(String name, String email, String specialization, int experience) {
        User user = userRepository.save(new User(name, email, passwordEncoder.encode("doctor123"), "DOCTOR"));
        Doctor doctor = doctorRepository.save(new Doctor(user, specialization, experience));

        // 4 slots of 30 minutes each, for tomorrow, day after, and the day after that
        LocalTime[] starts = { LocalTime.of(10, 0), LocalTime.of(10, 30), LocalTime.of(11, 0), LocalTime.of(11, 30) };
        for (int day = 1; day <= 3; day++) {
            LocalDate date = LocalDate.now().plusDays(day);
            for (LocalTime start : starts) {
                slotRepository.save(new Slot(doctor, date, start, start.plusMinutes(30)));
            }
        }
    }
}
