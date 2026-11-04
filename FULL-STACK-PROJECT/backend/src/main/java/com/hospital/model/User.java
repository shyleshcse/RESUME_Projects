package com.hospital.model;

import jakarta.persistence.*;

/**
 * One row in the "users" table.
 * EVERY person (patient, doctor, admin) is a User. The "role" column tells which one.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // database gives 1, 2, 3 ... automatically
    private Long id;

    private String name;

    @Column(unique = true, nullable = false) // no two users can have the same email
    private String email;

    private String password; // stored as a BCrypt hash, never plain text

    private String role;     // "PATIENT", "DOCTOR" or "ADMIN"

    public User() {}

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
