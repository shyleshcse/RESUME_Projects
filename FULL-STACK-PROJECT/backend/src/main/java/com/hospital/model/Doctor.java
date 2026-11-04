package com.hospital.model;

import jakarta.persistence.*;

/**
 * One row in the "doctors" table.
 * A doctor has a login (a User with role DOCTOR) plus extra info (specialization, experience).
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each doctor is linked to exactly one User (the doctor's login account)
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String specialization; // e.g. Cardiology

    private int experienceYears;

    public Doctor() {}

    public Doctor(User user, String specialization, int experienceYears) {
        this.user = user;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getSpecialization() { return specialization; }
    public int getExperienceYears() { return experienceYears; }
}
