# 03 - The Database

The project has **4 tables**. You do not need to create them. Spring Boot creates them from the Java classes in `model/` (this is what `spring.jpa.hibernate.ddl-auto=update` does).

> A copy of the SQL is in `database/schema.sql` **only so you can read it**. The app does not use that file.

## The 4 tables

```
 ┌────────────┐       ┌────────────┐
 │   users    │◄──────│  doctors   │       users   = everybody's login (patient/doctor/admin)
 │            │ 1   1 │            │       doctors = extra info for users who are doctors
 └─────┬──────┘       └─────┬──────┘
       │                    │ 1
       │                    │
       │                    │ many
       │              ┌─────▼──────┐
       │              │   slots    │       slots = bookable times of a doctor
       │              └─────┬──────┘
       │ 1                  │ 1
       │                    │
       │ many               │ many
       │              ┌─────▼──────┐
       └─────────────►│appointments│       appointments = "this patient booked this slot"
                      └────────────┘
```

Read the arrows like sentences:
- One **doctor** has many **slots**.
- One **patient (user)** has many **appointments**.
- One **slot** appears in appointments (one active booking at a time).

## Table details

### users
| Column | Type | Meaning |
|--------|------|---------|
| id | bigint, auto | Primary key |
| name | varchar | Full name |
| email | varchar, unique | Used for login |
| password | varchar | BCrypt hash |
| role | varchar | `PATIENT`, `DOCTOR`, `ADMIN` |

### doctors
| Column | Type | Meaning |
|--------|------|---------|
| id | bigint, auto | Primary key (this is the "doctorId" used in URLs) |
| user_id | bigint | **Foreign key** -> users.id (the doctor's login) |
| specialization | varchar | Cardiology, Dermatology ... |
| experience_years | int | Years of experience |

### slots
| Column | Type | Meaning |
|--------|------|---------|
| id | bigint, auto | Primary key |
| doctor_id | bigint | **Foreign key** -> doctors.id |
| slot_date | date | e.g. 2026-11-04 |
| start_time | time | e.g. 10:00 |
| end_time | time | e.g. 10:30 |
| booked | boolean | `false` = free, `true` = taken |

### appointments
| Column | Type | Meaning |
|--------|------|---------|
| id | bigint, auto | Primary key |
| patient_id | bigint | **Foreign key** -> users.id |
| slot_id | bigint | **Foreign key** -> slots.id |
| status | varchar | `BOOKED`, `CANCELLED`, `COMPLETED` |
| created_at | datetime | When it was booked |

## Key words (with examples)

- **Primary key (id):** like your **Aadhaar number**: unique for every row.
- **Foreign key (doctor_id):** like writing **"Doctor no. 1"** on a form instead of copying all of the doctor's details again. It *points* to a row in another table.
- **Unique (email):** two people cannot use the same email.

## Example data after the first start

**users**
| id | name | email | role |
|----|------|-------|------|
| 1 | Admin | admin@hospital.com | ADMIN |
| 2 | Test Patient | patient@test.com | PATIENT |
| 3 | Dr. Priya Sharma | priya@hospital.com | DOCTOR |
| 4 | Dr. Arun Kumar | arun@hospital.com | DOCTOR |
| 5 | Dr. Meena Iyer | meena@hospital.com | DOCTOR |

**doctors**
| id | user_id | specialization | experience_years |
|----|---------|----------------|------------------|
| 1 | 3 | Cardiology | 12 |
| 2 | 4 | Dermatology | 8 |
| 3 | 5 | Pediatrics | 10 |

**slots** (first rows; each doctor gets 12 slots: 4 per day for the next 3 days)
| id | doctor_id | slot_date | start_time | end_time | booked |
|----|-----------|-----------|------------|----------|--------|
| 1 | 1 | tomorrow | 10:00 | 10:30 | false |
| 2 | 1 | tomorrow | 10:30 | 11:00 | false |
| 3 | 1 | tomorrow | 11:00 | 11:30 | false |
| 4 | 1 | tomorrow | 11:30 | 12:00 | false |
| 5 | 1 | day after | 10:00 | 10:30 | false |
| ... | | | | | |

**appointments** starts empty. After patient books slot 1:
| id | patient_id | slot_id | status | created_at |
|----|-----------|---------|--------|------------|
| 1 | 2 | 1 | BOOKED | 2026-11-03 18:05:11 |

...and `slots` row 1 changes to `booked = true`.

## Why does an appointment need a "status"?

When a patient cancels, we **do not delete** the row. We set `status = CANCELLED`. This keeps history (a real hospital wants to know that someone cancelled). The slot gets `booked = false` so another patient can take it.

## Useful SQL queries to practise

```sql
-- All doctors with their names (JOIN two tables)
SELECT d.id, u.name, d.specialization
FROM doctors d JOIN users u ON d.user_id = u.id;

-- Free slots of doctor 1
SELECT * FROM slots WHERE doctor_id = 1 AND booked = false;

-- All appointments with patient and doctor names
SELECT a.id, p.name AS patient, du.name AS doctor, s.slot_date, s.start_time, a.status
FROM appointments a
JOIN users p      ON a.patient_id = p.id
JOIN slots s      ON a.slot_id = s.id
JOIN doctors d    ON s.doctor_id = d.id
JOIN users du     ON d.user_id = du.id;

-- How many appointments does each doctor have?
SELECT du.name, COUNT(*) AS total
FROM appointments a
JOIN slots s   ON a.slot_id = s.id
JOIN doctors d ON s.doctor_id = d.id
JOIN users du  ON d.user_id = du.id
GROUP BY du.name;
```

Next: [04-BACKEND-TUTORIAL.md](04-BACKEND-TUTORIAL.md)
