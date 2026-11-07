package com.cartnova.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Column(unique = true, nullable = false)
    public String email;
    @JsonIgnore
    public String password;          // BCrypt hash, never sent to the client
    public String role;              // CUSTOMER or ADMIN

    public User() {}
    public User(String name, String email, String password, String role) {
        this.name = name; this.email = email; this.password = password; this.role = role;
    }
}
