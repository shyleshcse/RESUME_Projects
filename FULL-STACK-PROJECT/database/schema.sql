-- FOR READING ONLY.
-- The application creates these tables automatically (spring.jpa.hibernate.ddl-auto=update).
-- This file shows what the 4 tables look like in MySQL.

CREATE DATABASE IF NOT EXISTS hospital_db;
USE hospital_db;

CREATE TABLE users (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255),
    email     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255),              -- BCrypt hash, never plain text
    role      VARCHAR(255)               -- PATIENT / DOCTOR / ADMIN
);

CREATE TABLE doctors (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT UNIQUE,
    specialization   VARCHAR(255),
    experience_years INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE slots (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id   BIGINT,
    slot_date   DATE,
    start_time  TIME,
    end_time    TIME,
    booked      BIT NOT NULL,            -- 0 = free, 1 = booked
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

CREATE TABLE appointments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id  BIGINT,
    slot_id     BIGINT,
    status      VARCHAR(255),            -- BOOKED / CANCELLED / COMPLETED
    created_at  DATETIME,
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (slot_id)    REFERENCES slots(id)
);
