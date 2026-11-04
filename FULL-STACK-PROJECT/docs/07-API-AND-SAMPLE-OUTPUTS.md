# 07 - API Reference and Sample Outputs

> **About the outputs on this page:** they show the **shape** of the JSON your backend returns,
> written from the code. Ids, dates and tokens on your computer will differ (dates are always
> "tomorrow" and the next 2 days from the day you start the app). Compare your real output with these.

Base URL: `http://localhost:8080`

## All APIs in one table

| Method | URL | Role | Purpose |
|--------|-----|------|---------|
| POST | `/api/auth/register` | public | Patient registers |
| POST | `/api/auth/login` | public | Login, get token |
| GET | `/api/doctors` | any logged-in | All doctors |
| GET | `/api/doctors?specialization=card` | any logged-in | Search doctors |
| GET | `/api/doctors/{id}` | any logged-in | One doctor |
| GET | `/api/doctors/{id}/slots` | any logged-in | Free slots of a doctor |
| POST | `/api/appointments` | PATIENT | Book a slot |
| GET | `/api/appointments/my` | PATIENT | My appointments |
| PUT | `/api/appointments/{id}/cancel` | PATIENT | Cancel |
| POST | `/api/doctor/slots` | DOCTOR | Add a slot |
| GET | `/api/doctor/slots` | DOCTOR | My slots |
| GET | `/api/doctor/appointments` | DOCTOR | Patients who booked me |
| PUT | `/api/doctor/appointments/{id}/complete` | DOCTOR | Mark completed |
| POST | `/api/admin/doctors` | ADMIN | Add a doctor |
| GET | `/api/admin/appointments` | ADMIN | All appointments |

## Test with Postman (easiest)
1. Postman -> **Import** -> choose `postman/Hospital-Appointment.postman_collection.json`
2. Open folder **1. Login** and send *Login as patient*. The token is saved automatically into a variable.
3. Now send any patient request. The token is attached for you.
4. To test as doctor/admin, send *Login as doctor* / *Login as admin* first (it replaces the saved token).

## Test with curl (Mac/Linux/Git Bash)

```bash
# 1. Login and keep the token in a variable
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"patient@test.com","password":"patient123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# 2. Use it
curl -s http://localhost:8080/api/doctors -H "Authorization: Bearer $TOKEN"
```
(On Windows CMD, use Postman or Git Bash.)

---

## 1. Register  `POST /api/auth/register`

Request:
```json
{ "name": "Ravi Kumar", "email": "ravi@test.com", "password": "secret123" }
```
Response `200`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2Iiwicm9sZSI6IlBBVElFTlQi...",
  "userId": 6,
  "name": "Ravi Kumar",
  "email": "ravi@test.com",
  "role": "PATIENT"
}
```
Errors:
```json
409  { "error": "This email is already registered" }
400  { "error": "Password must be at least 6 characters" }
400  { "error": "Name, email and password are required" }
```

## 2. Login  `POST /api/auth/login`

Request:
```json
{ "email": "patient@test.com", "password": "patient123" }
```
Response `200`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "userId": 2,
  "name": "Test Patient",
  "email": "patient@test.com",
  "role": "PATIENT"
}
```
Error:
```json
401  { "error": "Wrong email or password" }
```

## 3. List / search doctors  `GET /api/doctors`  (header: `Authorization: Bearer <token>`)

Response `200`:
```json
[
  { "id": 1, "name": "Dr. Priya Sharma", "email": "priya@hospital.com", "specialization": "Cardiology",  "experienceYears": 12 },
  { "id": 2, "name": "Dr. Arun Kumar",   "email": "arun@hospital.com",  "specialization": "Dermatology", "experienceYears": 8 },
  { "id": 3, "name": "Dr. Meena Iyer",   "email": "meena@hospital.com", "specialization": "Pediatrics",  "experienceYears": 10 }
]
```
`GET /api/doctors?specialization=card` ->
```json
[
  { "id": 1, "name": "Dr. Priya Sharma", "email": "priya@hospital.com", "specialization": "Cardiology", "experienceYears": 12 }
]
```

## 4. Free slots of a doctor  `GET /api/doctors/1/slots`

Response `200` (dates = tomorrow and after; booked slots are not listed):
```json
[
  { "id": 1, "doctorId": 1, "doctorName": "Dr. Priya Sharma", "date": "2026-11-04", "startTime": "10:00:00", "endTime": "10:30:00", "booked": false },
  { "id": 2, "doctorId": 1, "doctorName": "Dr. Priya Sharma", "date": "2026-11-04", "startTime": "10:30:00", "endTime": "11:00:00", "booked": false },
  { "id": 3, "doctorId": 1, "doctorName": "Dr. Priya Sharma", "date": "2026-11-04", "startTime": "11:00:00", "endTime": "11:30:00", "booked": false }
]
```
(There are 12 in total; shortened here. The React page shows times as `10:00`.)

Error for an unknown doctor:
```json
404  { "error": "Doctor not found" }
```

## 5. Book  `POST /api/appointments`  (PATIENT token)

Request:
```json
{ "slotId": 1 }
```
Response `200`:
```json
{
  "id": 1,
  "patientName": "Test Patient",
  "doctorName": "Dr. Priya Sharma",
  "specialization": "Cardiology",
  "date": "2026-11-04",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "status": "BOOKED"
}
```
Booking the same slot again:
```json
409  { "error": "Sorry, this slot was just booked by someone else. Please pick another slot." }
```
Other errors:
```json
404  { "error": "Slot not found" }
400  { "error": "This slot is in the past" }
403  { "error": "Only patients can do this" }        <- you used a doctor/admin token
401  { "error": "Please login first" }               <- no token
```

## 6. My appointments  `GET /api/appointments/my`

```json
[
  {
    "id": 1,
    "patientName": "Test Patient",
    "doctorName": "Dr. Priya Sharma",
    "specialization": "Cardiology",
    "date": "2026-11-04",
    "startTime": "10:00:00",
    "endTime": "10:30:00",
    "status": "BOOKED"
  }
]
```

## 7. Cancel  `PUT /api/appointments/1/cancel`

```json
{
  "id": 1, "patientName": "Test Patient", "doctorName": "Dr. Priya Sharma",
  "specialization": "Cardiology", "date": "2026-11-04",
  "startTime": "10:00:00", "endTime": "10:30:00", "status": "CANCELLED"
}
```
Errors:
```json
403  { "error": "This is not your appointment" }
400  { "error": "Only BOOKED appointments can be cancelled (this one is CANCELLED)" }
404  { "error": "Appointment not found" }
```

## 8. Doctor adds a slot  `POST /api/doctor/slots`  (DOCTOR token)

Request:
```json
{ "date": "2026-11-05", "startTime": "14:00", "endTime": "14:30" }
```
Response `200`:
```json
{ "id": 37, "doctorId": 1, "doctorName": "Dr. Priya Sharma", "date": "2026-11-05", "startTime": "14:00:00", "endTime": "14:30:00", "booked": false }
```
Errors:
```json
409  { "error": "You already have a slot at this date and time" }
400  { "error": "You cannot add a slot in the past" }
400  { "error": "Start time must be before end time" }
400  { "error": "Invalid request data. Check the fields and date/time format." }
```

## 9. Doctor's slots  `GET /api/doctor/slots`
Same shape as the slot list above, but includes **booked** ones (`"booked": true`).

## 10. Doctor's appointments  `GET /api/doctor/appointments`

```json
[
  {
    "id": 1,
    "patientName": "Test Patient",
    "doctorName": "Dr. Priya Sharma",
    "specialization": "Cardiology",
    "date": "2026-11-04",
    "startTime": "10:00:00",
    "endTime": "10:30:00",
    "status": "BOOKED"
  }
]
```

## 11. Mark completed  `PUT /api/doctor/appointments/1/complete`
Same JSON with `"status": "COMPLETED"`.
Errors: `403 "This appointment is not with you"`, `400 "Only BOOKED appointments can be completed (this one is CANCELLED)"`.

## 12. Admin adds a doctor  `POST /api/admin/doctors`  (ADMIN token)

Request:
```json
{
  "name": "Dr. Ravi Menon",
  "email": "ravi.menon@hospital.com",
  "password": "doctor123",
  "specialization": "Neurology",
  "experienceYears": 6
}
```
Response `200`:
```json
{ "id": 4, "name": "Dr. Ravi Menon", "email": "ravi.menon@hospital.com", "specialization": "Neurology", "experienceYears": 6 }
```
Errors: `409 "This email is already registered"`, `400 "Name, email, password and specialization are required"`.

## 13. Admin: all appointments  `GET /api/admin/appointments`
Same shape as "My appointments" but for every patient, newest first.

---

## What the console prints (backend)

Because `spring.jpa.show-sql=true`, each API call prints the SQL. Booking a slot prints roughly:

```
select u1_0.id, u1_0.email, u1_0.name, u1_0.password, u1_0.role from users u1_0 where u1_0.id=?
select s1_0.id, s1_0.booked, s1_0.doctor_id, s1_0.end_time, s1_0.slot_date, s1_0.start_time from slots s1_0 where s1_0.id=? for update
update slots set booked=?, doctor_id=?, end_time=?, slot_date=?, start_time=? where id=?
insert into appointments (created_at, patient_id, slot_id, status) values (?, ?, ?, ?)
```
Notice the **`for update`** at the end of the second line. That is the row lock that prevents double booking. (Exact text depends on the database and version.)

## HTTP status code cheat sheet

| Code | Meaning in this project |
|------|-------------------------|
| 200 | Success |
| 400 | You sent wrong or missing data |
| 401 | Not logged in / token expired / wrong password |
| 403 | Logged in, but wrong role or not your data |
| 404 | Doctor / slot / appointment does not exist |
| 409 | Conflict: email already used, slot already booked |

Next: [08-INTERVIEW-QUESTIONS.md](08-INTERVIEW-QUESTIONS.md)
