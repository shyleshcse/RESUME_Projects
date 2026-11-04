# 06 - Workflows (Step by Step)

Each workflow shows **what you do**, **what React sends**, **what the backend does**, and **what changes in the database**.

---

## Workflow 1 - A complete day in the hospital (story to try yourself)

Run the project and follow the story with 3 browser windows (use one normal window + 2 incognito windows so logins do not clash).

| Step | Who | Action | What you should see |
|------|-----|--------|---------------------|
| 1 | Patient | Register a new account (or login `patient@test.com`) | Lands on **Find Doctors** with 3 doctor cards |
| 2 | Patient | Type `card` in search -> Search | Only **Dr. Priya Sharma (Cardiology)** |
| 3 | Patient | **View Slots** | A table of free slots (tomorrow and next 2 days) |
| 4 | Patient | Click **Book** on tomorrow 10:00 | Green message "Appointment booked!" and that slot **disappears** from the list |
| 5 | Patient | **My Appointments** | One row, status **BOOKED** (yellow) |
| 6 | Doctor | Login `priya@hospital.com` | **Doctor Dashboard** |
| 7 | Doctor | Look at "Patients who booked me" | The patient's name with status BOOKED; in "My slots" that time shows **Booked** |
| 8 | Doctor | Add slot: tomorrow 14:00 - 14:30 | Green "Slot added!" and the new slot appears as **Free** |
| 9 | Doctor | **Mark Completed** | Status becomes **COMPLETED** (green) |
| 10 | Patient | Book another slot, then **Cancel** it | Status **CANCELLED** (red), and the slot is **free again** in View Slots |
| 11 | Admin | Login `admin@hospital.com` -> add doctor "Dr. Ravi" / Neurology | Doctor appears in the table. Now login as him with the same email and password |
| 12 | Admin | Look at "All appointments" | Every booking from every patient |

---

## Workflow 2 - Login (with JWT)

```
 React (Login.jsx)                     Backend                                 Database
 ─────────────────                     ───────                                 ────────
 user types email + password
 POST /api/auth/login
 { "email": "...", "password": "..." } ───►
                                       AuthInterceptor: path is /api/auth/ -> skip check
                                       AuthController.login()
                                       AuthService.login():
                                         userRepository.findByEmail(...) ───────────► SELECT * FROM users WHERE email=?
                                                                                ◄─────  the row (with password HASH)
                                         passwordEncoder.matches(typed, hash)
                                           no  -> 401 { "error": "Wrong email or password" }
                                           yes -> JwtUtil.generateToken(user)
                                    ◄─── { token, userId, name, email, role }
 App.jsx: save in localStorage + state
 go to home page of the role
```

**Every later request:**
```
 GET /api/doctors
 Header:  Authorization: Bearer eyJhbGciOi...        (added by api.js automatically)
                              │
                              ▼
 AuthInterceptor reads the token -> valid? -> sets userId + role -> controller runs
```

---

## Workflow 3 - Booking an appointment

```
 DoctorSlots.jsx: patient clicks [Book] on slot 1
        │  POST /api/appointments   { "slotId": 1 }   + token
        ▼
 AuthInterceptor ─ valid token? role == PATIENT? ─ no -> 401/403
        ▼
 AppointmentController.book()   (userId read from the token)
        ▼
 AppointmentService.book()   @Transactional
   1. find the patient user
   2. LOCK slot 1 and read it            (SELECT ... FOR UPDATE)
   3. slot.booked already true?   ───yes──► 409 "Sorry, this slot was just booked by someone else"
   4. slot.booked = true          (UPDATE slots SET booked = true WHERE id = 1)
   5. INSERT INTO appointments (patient_id, slot_id, status, created_at)
                               VALUES (2, 1, 'BOOKED', now())
   6. commit (lock released)
        ▼
 JSON response -> React shows the green message and reloads the slots
```

### Database before and after

| | slots row 1 | appointments |
|---|-------------|--------------|
| Before | `booked = false` | (empty) |
| After | `booked = true` | `id=1, patient_id=2, slot_id=1, status=BOOKED` |

---

## Workflow 4 - Double-booking protection (the interview favourite)

**Problem.** Patient A and Patient B open the same doctor's page. Both see "10:00 free". Both click **Book** at the same second.

**Cinema example:** the last seat. If the counter staff serve both people at the same moment, both get a ticket for the same seat. Chaos.

**Without protection (the bug):**

```
 Time │ Patient A                       │ Patient B
 ─────┼─────────────────────────────────┼─────────────────────────────────
  1   │ reads slot: booked = false      │
  2   │                                 │ reads slot: booked = false
  3   │ sets booked = true, saves       │
  4   │                                 │ sets booked = true, saves   <- BOTH booked!
```

**With our protection** (`findByIdForUpdate` = row lock):

```
 Time │ Patient A                           │ Patient B
 ─────┼─────────────────────────────────────┼──────────────────────────────────────
  1   │ LOCK slot 1, reads booked = false   │
  2   │                                     │ tries to LOCK slot 1 -> must WAIT...
  3   │ sets booked = true, saves, COMMIT   │   (still waiting)
  4   │ lock released                       │ gets the lock, reads booked = TRUE
  5   │                                     │ -> 409 "slot was just booked by someone else"
```

The code that does it:

```java
// SlotRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)          // "SELECT ... FOR UPDATE"
@Query("select s from Slot s where s.id = :id")
Optional<Slot> findByIdForUpdate(@Param("id") Long id);

// AppointmentService.book()  (runs inside @Transactional, so the lock lasts until the end)
Slot slot = slotRepository.findByIdForUpdate(slotId)...;
if (slot.isBooked()) { throw new ApiException(HttpStatus.CONFLICT, "..."); }
slot.setBooked(true);
```

**One-line interview answer:** "I lock the slot row with a pessimistic write lock inside a transaction, then check the `booked` flag. The second request waits for the first to commit, sees `booked = true`, and gets a 409 Conflict."

**How to see it yourself:** open the same doctor page in two browser windows as two different patients, click **Book** on the same slot in window 1, then click **Book** on the same (stale) slot in window 2. Window 2 shows the red message.

---

## Workflow 5 - Cancelling an appointment

```
 MyAppointments.jsx: [Cancel] on appointment 7
        │  PUT /api/appointments/7/cancel + token
        ▼
 AppointmentService.cancel()  @Transactional
   - appointment exists?                      no -> 404
   - appointment.patient == me?               no -> 403 "This is not your appointment"
   - status == BOOKED?                        no -> 400 "Only BOOKED appointments can be cancelled"
   - status = CANCELLED          (UPDATE appointments ...)
   - slot.booked = false         (UPDATE slots ...)  <- the slot is free for others again
```

The appointment row is kept (history). Only its status changes.

---

## Workflow 6 - Doctor adds a slot

```
 DoctorDashboard.jsx: date + start + end -> [Add Slot]
        │  POST /api/doctor/slots { "date":"2026-11-05", "startTime":"14:00", "endTime":"14:30" }
        ▼
 AuthInterceptor: role must be DOCTOR (path /api/doctor/)
        ▼
 DoctorService.addSlot(userId from token, ...)
   - find the doctor profile of this user
   - date in the past?                    -> 400
   - start not before end?                -> 400
   - same doctor + date + start exists?   -> 409
   - INSERT INTO slots (...) booked = false
```

---

## Workflow 7 - Admin adds a doctor

```
 AdminDashboard.jsx -> POST /api/admin/doctors
   { "name":"Dr. Ravi", "email":"ravi@hospital.com", "password":"doctor123",
     "specialization":"Neurology", "experienceYears":6 }
        ▼
 AuthInterceptor: role must be ADMIN
        ▼
 DoctorService.addDoctor()  @Transactional
   - all fields present? password >= 6?      else 400
   - email unused?                           else 409
   - INSERT INTO users   (role = DOCTOR, password = BCrypt hash)
   - INSERT INTO doctors (user_id = new user, specialization, experience_years)
   - both inserts succeed together, or neither
```
Now Dr. Ravi can login with `ravi@hospital.com / doctor123` and add his own slots.

---

## Workflow 6B - What happens when things go wrong (try these!)

| Try this | What you will see | Why |
|----------|-------------------|-----|
| Login with a wrong password | Red "Wrong email or password" | AuthService -> 401 |
| Register with an existing email | "This email is already registered" | 409 |
| Register with a 3-letter password | "Password must be at least 6 characters" | 400 |
| Patient opens `http://localhost:5173/admin` | Redirected to the patient home | ProtectedRoute |
| Call an admin API with a patient token (Postman) | 403 "Only admin can do this" | AuthInterceptor |
| Call any API with no token | 401 "Please login first" | AuthInterceptor |
| Doctor adds the same slot twice | "You already have a slot at this date and time" | 409 |
| Doctor adds a slot in the past | "You cannot add a slot in the past" | 400 |
| Patient A tries to cancel Patient B's appointment (Postman) | 403 "This is not your appointment" | ownership check |
| Cancel an already cancelled appointment | 400 "Only BOOKED appointments can be cancelled..." | status check |

Next: [07-API-AND-SAMPLE-OUTPUTS.md](07-API-AND-SAMPLE-OUTPUTS.md)
