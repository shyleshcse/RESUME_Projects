-- FOR READING ONLY. The app inserts demo data itself (config/DataLoader.java).
-- Passwords cannot be inserted as plain text by hand because the app compares BCrypt hashes.
-- Use the app's Register page or the DataLoader to create real accounts.

-- Handy queries to run after you have used the app:

SELECT * FROM users;
SELECT * FROM doctors;
SELECT * FROM slots WHERE booked = 0 LIMIT 10;
SELECT * FROM appointments;

-- Appointments with names
SELECT a.id, p.name AS patient, du.name AS doctor, d.specialization,
       s.slot_date, s.start_time, s.end_time, a.status
FROM appointments a
JOIN users p   ON a.patient_id = p.id
JOIN slots s   ON a.slot_id = s.id
JOIN doctors d ON s.doctor_id = d.id
JOIN users du  ON d.user_id = du.id
ORDER BY a.id DESC;
