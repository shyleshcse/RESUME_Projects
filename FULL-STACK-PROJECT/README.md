# 🏥 Hospital Appointment Booking System

A full-stack web application where **patients** find doctors and book appointment slots online, **doctors** manage their own time slots, and an **admin** adds doctors and monitors all appointments. Built with **React**, **Spring Boot**, **MySQL** and **JWT** authentication.

---

## 📌 About the Project

This project models a real hospital booking flow with three user roles. Each role has its own login, its own pages and its own protected APIs.

The key feature is **double-booking protection**. If two patients click the same slot at the same moment, the database row is locked, so only one of them can book it and the other gets a clear message.

---

## ✨ Features

### 👤 Patient
- Register and log in
- Search doctors by specialization
- View a doctor's free slots
- Book an appointment
- View appointment history
- Cancel a booked appointment (the slot becomes free again)

### 🩺 Doctor
- Log in to a personal dashboard
- Add time slots (date, start time, end time)
- See which patient booked which slot
- Mark an appointment as completed

### 🛡️ Admin
- Log in to the admin dashboard
- Add new doctors (creates the login and the doctor profile together)
- View all doctors and all appointments

### 🔒 Security and Reliability
- **JWT login** (token valid for 24 hours)
- **Role-based access control**: a patient cannot open admin or doctor APIs
- **BCrypt** password hashing (passwords are never stored as plain text)
- **Double-booking protection** using a pessimistic database lock (`SELECT ... FOR UPDATE`)
- Clear error messages with proper HTTP status codes (400, 401, 403, 404, 409)

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, React Router 6, Axios, Vite 5 |
| Backend | Java 17, Spring Boot 3.3.5, Spring Data JPA (Hibernate) |
| Database | MySQL (or in-memory H2 for quick testing) |
| Authentication | JWT (jjwt 0.12.6), BCrypt (spring-security-crypto) |
| Build Tools | Maven, npm |
| API Testing | Postman |

---

## 🏗️ Architecture

```
React (port 5173)  ──/api──▶  Spring Boot (port 8080)  ──JPA──▶  MySQL
   pages, forms               Controller → Service → Repository
```

The backend follows a layered design:

| Layer | Purpose |
|-------|---------|
| **Controller** | Defines the API URLs and receives requests |
| **Service** | Business logic (booking rules, validation) |
| **Repository** | Talks to the database using Spring Data JPA |
| **Model** | Java classes that map to database tables |
| **DTO** | Shapes of the JSON sent and received |
| **Security** | JWT creation and an interceptor that checks the token and role |
| **Exception** | One global handler that turns errors into clean JSON responses |

The Vite dev server forwards every `/api` request to the backend, so the frontend can call `api.get("/doctors")` without CORS problems.

---

## 🗄️ Database Design

| Table | Purpose | Key Columns |
|-------|---------|-------------|
| `users` | Login accounts for all roles | id, name, email (unique), password (hash), role |
| `doctors` | Doctor profile linked to a user | id, user_id, specialization, experience_years |
| `slots` | Time slots created by doctors | id, doctor_id, slot_date, start_time, end_time, booked |
| `appointments` | A patient's booking of a slot | id, patient_id, slot_id, status, created_at |

**Relationships**
- One `user` (role DOCTOR) has one `doctor` profile
- One `doctor` has many `slots`
- One `slot` has at most one active `appointment`
- One `user` (role PATIENT) has many `appointments`

**Appointment status flow**

```
BOOKED ──▶ CANCELLED   (patient cancels, slot is freed)
BOOKED ──▶ COMPLETED   (doctor marks it done)
```

Tables are created automatically by Hibernate on the first run. The SQL files in the `database/` folder are for reference.

---

## 🔌 API Endpoints

### Auth (public)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Register a new patient |
| POST | `/api/auth/login` | Log in and receive a JWT token |

### Doctors (any logged-in user)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/doctors?specialization=` | List or search doctors |
| GET | `/api/doctors/{id}` | Get one doctor |
| GET | `/api/doctors/{id}/slots` | Get a doctor's free slots |

### Patient (role: PATIENT)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/appointments` | Book a slot |
| GET | `/api/appointments/my` | View my appointments |
| PUT | `/api/appointments/{id}/cancel` | Cancel my appointment |

### Doctor (role: DOCTOR)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/doctor/slots` | Add a slot |
| GET | `/api/doctor/slots` | View my slots |
| GET | `/api/doctor/appointments` | View my appointments |
| PUT | `/api/doctor/appointments/{id}/complete` | Mark an appointment completed |

### Admin (role: ADMIN)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/admin/doctors` | Add a doctor |
| GET | `/api/admin/appointments` | View all appointments |

Protected APIs need this header:

```
Authorization: Bearer <your-token>
```

A ready-made Postman collection is available in `postman/Hospital-Appointment.postman_collection.json`.

---

## 📏 Business Rules

- New users can only register as **PATIENT**. Doctors are created by the admin.
- Password must be at least **6 characters**.
- Email must be **unique** (stored in lowercase).
- A doctor cannot add a slot in the **past**.
- A slot's start time must be **before** its end time.
- A doctor cannot create two slots at the **same date and time**.
- A slot in the past cannot be booked.
- A slot that is already booked returns **409 Conflict**.
- Only a **BOOKED** appointment can be cancelled or completed.
- A patient can cancel only **their own** appointments, and a doctor can complete only appointments **with them**.

---

## 🔐 How Double-Booking Is Prevented

Booking runs inside one database transaction:

1. **Lock** the slot row (`SELECT ... FOR UPDATE`). A second patient clicking at the same time must wait.
2. **Check** if the slot is already booked. If yes, return a 409 error.
3. **Mark** the slot as booked.
4. **Save** the appointment with status `BOOKED`.

---

## 📁 Project Structure

```
hospital-appointment-system/
├── backend/                       # Spring Boot (Java)
│   ├── pom.xml
│   └── src/main/
│       ├── resources/             # application.properties, application-h2.properties
│       └── java/com/hospital/
│           ├── HospitalApplication.java
│           ├── controller/        # Auth, Doctor, Appointment, DoctorPanel, Admin
│           ├── service/           # AuthService, DoctorService, AppointmentService
│           ├── repository/        # User, Doctor, Slot, Appointment
│           ├── model/             # User, Doctor, Slot, Appointment
│           ├── dto/               # Request and response shapes
│           ├── security/          # JwtUtil, AuthInterceptor
│           ├── exception/         # ApiException, GlobalExceptionHandler
│           └── config/            # WebConfig, DataLoader (demo data)
├── frontend/                      # React (Vite)
│   └── src/
│       ├── components/            # Navbar, ProtectedRoute
│       ├── pages/                 # Login, Register, Doctors, DoctorSlots,
│       │                          # MyAppointments, DoctorDashboard, AdminDashboard
│       ├── api.js                 # Axios setup
│       ├── App.jsx                # Routes
│       └── main.jsx
├── database/                      # schema.sql and sample-data.sql (reference)
├── postman/                       # Postman collection
├── docs/                          # Step-by-step tutorials
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or later
- Maven
- Node.js 18 or later
- MySQL (optional, only for Option B)

### Option A: Quick start without MySQL (H2 in-memory database)

**Terminal 1: Backend**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

**Terminal 2: Frontend**
```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

> The H2 database lives in memory, so data resets every time the backend stops. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:hospital_db`, user: `sa`, empty password).

### Option B: Run with MySQL

1. Make sure MySQL is running on `localhost:3306`.
2. Set your MySQL password. Either edit `backend/src/main/resources/application.properties` or set environment variables:
   ```bash
   # Linux / macOS
   export DB_USER=root
   export DB_PASSWORD=your_password

   # Windows (PowerShell)
   $env:DB_USER="root"
   $env:DB_PASSWORD="your_password"
   ```
3. Start the backend (the `hospital_db` database is created automatically):
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## 🔑 Demo Logins (created automatically on first start)

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@hospital.com | admin123 |
| Doctor | priya@hospital.com | doctor123 |
| Patient | patient@test.com | patient123 |

Other doctors: `arun@hospital.com` and `meena@hospital.com` (password `doctor123`).

Demo data includes 3 doctors (Cardiology, Dermatology, Pediatrics) with four 30-minute slots a day for the next 3 days.

---

## 🧪 Testing the APIs

1. Open Postman and import `postman/Hospital-Appointment.postman_collection.json`.
2. Call **Login** and copy the token from the response.
3. Add the token as a Bearer token and call the other APIs.

---

## 📜 Available Scripts

**Frontend**

| Command | Description |
|---------|-------------|
| `npm run dev` | Start the development server |
| `npm run build` | Create a production build |
| `npm run preview` | Preview the production build |

**Backend**

| Command | Description |
|---------|-------------|
| `mvn spring-boot:run` | Run with MySQL |
| `mvn spring-boot:run -Dspring-boot.run.profiles=h2` | Run with in-memory H2 |
| `mvn clean package` | Build the JAR file |

---

## 🔮 Future Improvements

- Email or SMS reminders for appointments
- Payment integration for consultation fees
- Doctor profile photos and patient medical history
- Reschedule appointments
- Move the JWT secret to an environment variable
- Unit and integration tests (JUnit, React Testing Library)
- Docker setup for one-command start
- Deploy the frontend and backend online

---

## 👨‍💻 Author

**Shyleshkanna B**
GitHub: [@shyleshcse](https://github.com/shyleshcse)

---

⭐ If you like this project, give it a star!
