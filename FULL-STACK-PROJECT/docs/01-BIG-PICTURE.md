# 01 - The Big Picture

## What does this project do?

Think of a **hospital reception desk**, but online.

| Real life | In this project |
|-----------|-----------------|
| A patient walks in and asks "Which doctors do you have?" | Patient page **Find Doctors** |
| Receptionist shows the doctor's free timings | Page **View Slots** |
| Receptionist writes the patient's name next to a timing | **Book** button -> saved in the database |
| That timing is now taken, nobody else can have it | Slot is marked `booked = true` |
| Patient calls to cancel | **Cancel** button -> slot becomes free again |
| Doctor checks his list for today | Doctor Dashboard |
| Hospital manager hires a new doctor | Admin Panel -> **Add Doctor** |

## The 3 parts of the project

```
  ┌────────────────┐    HTTP + JSON     ┌──────────────────┐    SQL     ┌──────────────┐
  │   FRONTEND     │ ─────────────────► │     BACKEND      │ ─────────► │   DATABASE   │
  │   React.js     │ ◄───────────────── │   Spring Boot    │ ◄───────── │    MySQL     │
  │ (what you see) │                    │ (rules + logic)  │            │ (saved data) │
  └────────────────┘                    └──────────────────┘            └──────────────┘
   runs on :5173                          runs on :8080
```

**Restaurant example:**

- **Frontend (React)** = the **menu and the waiter**. You see the menu, you place an order.
- **Backend (Spring Boot)** = the **kitchen**. It follows the rules and prepares the food.
- **Database (MySQL)** = the **storeroom**. All ingredients (data) are kept here safely.

The waiter never goes into the storeroom directly. React never touches MySQL directly either. It always asks the backend.

## What is "HTTP + JSON"?

When you press **Book**, React sends a small message to the backend:

```
POST http://localhost:8080/api/appointments
Body (JSON): { "slotId": 1 }
```

- **HTTP** = the language of the web (GET = read, POST = create, PUT = update).
- **JSON** = the format of the data: `{ "key": "value" }`.
- **API** = the list of URLs the backend offers. Example: `/api/doctors`.

The backend replies with JSON too:

```json
{ "id": 1, "doctorName": "Dr. Priya Sharma", "status": "BOOKED" }
```

## How the backend is organised (layers)

Every request travels through the same layers. This is the most important picture in the project:

```
 React button click
        │
        ▼
 ┌──────────────────┐
 │ AuthInterceptor  │  Security guard: is there a valid token? is the role allowed?
 └────────┬─────────┘
          ▼
 ┌──────────────────┐
 │   Controller     │  Front desk: receives the request, passes it on, returns the answer
 └────────┬─────────┘
          ▼
 ┌──────────────────┐
 │    Service       │  The brain: all rules (is the slot free? is this your appointment?)
 └────────┬─────────┘
          ▼
 ┌──────────────────┐
 │   Repository     │  Database worker: save / find / update rows
 └────────┬─────────┘
          ▼
      MySQL tables
```

**Office example:** Security guard (checks your ID card) -> Reception (takes your form) -> Manager (decides) -> Clerk (updates the register/files).

Why split like this? Each file has **one job**, so when something breaks you know exactly where to look.

## The 3 roles

| Role | Created how | Can do |
|------|-------------|--------|
| **PATIENT** | Registers on the website | Search doctors, book, cancel, see own appointments |
| **DOCTOR** | Admin creates the account | Add slots, see own bookings, mark completed |
| **ADMIN** | Created automatically at first start | Add doctors, see everything |

## Key ideas explained with real-life examples

### 1. JWT token (login)
Like a **wristband at a music festival**.
- At the gate (login) your ticket (email + password) is checked once.
- You get a wristband (token). It has your name and role, and a stamp nobody can fake.
- Inside, you just show the wristband. React adds it to every request automatically.
- After 24 hours the wristband stops working, so you login again.

### 2. Password hashing (BCrypt)
Like a **paper shredder**. We never keep your real password `doctor123`.
We keep the shredded version `$2a$10$N9qo8...`. At login we shred what you typed and compare the shredded pieces. Even if someone steals the database, they cannot read your password.

### 3. Roles
Like **colour-coded ID cards** in a hospital. Green card = patient, white coat card = doctor, red card = admin. Each door only opens for the right colour.

### 4. JPA / Repository
Normally you write SQL: `SELECT * FROM users WHERE email = 'a@b.com'`.
With JPA you write a Java method name `findByEmail("a@b.com")` and Spring writes the SQL for you. Like telling an assistant **"bring me the file for this email"** instead of walking to the cabinet.

### 5. Double-booking protection
Like **two people reaching for the last seat in a cinema at the same second**.
The ticket counter lets only one person at the counter at a time. The first person gets the seat, the second one is told "sorry, taken". Full explanation in [06-WORKFLOWS.md](06-WORKFLOWS.md).

## What is intentionally NOT used (to keep it simple)

| Skipped | Why |
|---------|-----|
| Spring Security filter chain | Too many hidden parts. We use one easy `AuthInterceptor` instead (about 50 lines). |
| Lombok | Hides getters/setters. We write them, so you see everything. |
| Redux / Context API | Plain `useState` is enough. |
| TypeScript | Plain JavaScript is easier. |
| Microservices, Docker | Not needed for learning. |

> In real companies Spring Security is common. After you understand this project, learning it will be much easier. Interview answer for this is in [08-INTERVIEW-QUESTIONS.md](08-INTERVIEW-QUESTIONS.md).

Next: [02-SETUP-AND-RUN.md](02-SETUP-AND-RUN.md)
