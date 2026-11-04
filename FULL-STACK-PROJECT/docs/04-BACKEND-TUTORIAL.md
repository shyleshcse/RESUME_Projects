# 04 - Backend Tutorial (Spring Boot)

Open the `backend/src/main/java/com/hospital/` folder next to this file and read along. Every Java file also has comments inside.

## Folder map

```
com/hospital/
├── HospitalApplication.java     1. START POINT
├── model/                       2. Java classes = database tables
│   ├── User.java
│   ├── Doctor.java
│   ├── Slot.java
│   └── Appointment.java
├── repository/                  3. Talk to the database
├── dto/Dto.java                 4. JSON shapes
├── service/                     5. Business logic (the brain)
├── controller/                  6. API URLs (the front desk)
├── security/                    7. JWT + role checking
├── exception/                   8. Nice error messages
└── config/                      9. Settings + demo data
```

Read in the order of the numbers. Each step uses the one before.

---

## 1. HospitalApplication.java - the start point

```java
@SpringBootApplication
public class HospitalApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
    }
}
```
`@SpringBootApplication` tells Spring: "scan this package, find all my classes, and wire them together." You never write `new AuthService()` yourself. Spring does it. This is called **Dependency Injection**.

**Example:** Spring is like a **hotel manager**. You say "I need a cleaner" (constructor parameter) and the manager sends one. You do not hire them yourself.

---

## 2. model/ - classes that become tables

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    ...
}
```

| Annotation | Meaning |
|-----------|---------|
| `@Entity` | "This class is a table" |
| `@Table(name="users")` | Table name |
| `@Id` | Primary key column |
| `@GeneratedValue` | Database fills 1, 2, 3 ... automatically |
| `@Column(unique = true)` | No duplicates |
| `@ManyToOne` | Many rows here point to ONE row there (many slots -> one doctor) |
| `@OneToOne` | One doctor <-> one user account |
| `@JoinColumn(name="doctor_id")` | The foreign key column name |

One field = one column. One object = one row.

---

## 3. repository/ - database workers

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

It is an **interface with no code**. Spring generates the code at startup from the method names.

| Method name | SQL Spring writes |
|-------------|-------------------|
| `findByEmail(e)` | `SELECT * FROM users WHERE email = e` |
| `existsByEmail(e)` | `SELECT COUNT(*) > 0 FROM users WHERE email = e` |
| `findBySpecializationContainingIgnoreCase(t)` | `... WHERE LOWER(specialization) LIKE '%t%'` |

Free methods from `JpaRepository`: `save()`, `findById()`, `findAll()`, `count()`, `deleteById()`.

For harder queries we write the query ourselves with `@Query` (see `SlotRepository`):
```java
@Query("select s from Slot s where s.doctor.id = :doctorId and s.booked = false and s.slotDate >= :today order by s.slotDate, s.startTime")
```
This is **JPQL**: like SQL, but with class and field names (`Slot`, `slotDate`) instead of table and column names.

`Optional<User>` means "maybe a user, maybe nothing". We then write `.orElseThrow(...)` to handle the "nothing" case.

---

## 4. dto/Dto.java - JSON shapes

A **DTO** is a small data box. We do not send the `User` entity to React because it contains the password hash!

```java
public record LoginRequest(String email, String password) {}
public record AuthResponse(String token, Long userId, String name, String email, String role) {}
```

When React sends `{"email":"a@b.com","password":"123456"}`, Spring converts it to a `LoginRequest` object (this conversion is done by a library called **Jackson**). Same in reverse for responses.

---

## 5. service/ - the brain

All the rules live here. Three services:

### AuthService
- `register()`: validate -> check email is new -> hash the password -> save as PATIENT -> return token
- `login()`: find user by email -> compare password hash -> return token

```java
passwordEncoder.encode("patient123")            // when saving
passwordEncoder.matches("patient123", savedHash) // when logging in
```

### DoctorService
- `addDoctor()` (admin): saves a **User** (role DOCTOR) **and** a **Doctor** row. `@Transactional` means "both succeed or both are cancelled".
- `searchDoctors()`: all doctors, or filtered by specialization
- `getAvailableSlots()`: free slots from today onwards
- `addSlot()` (doctor): checks date not in past, start < end, no duplicate, then saves
- `getMySlots()` (doctor)

### AppointmentService
- `book()`: **the most important method** (see below)
- `cancel()`: only the owner, only if status is BOOKED, frees the slot
- `complete()`: only the doctor of that appointment, only if BOOKED
- `getMyAppointments()`, `getDoctorAppointments()`, `getAllAppointments()`

#### The `book()` method line by line

```java
@Transactional
public AppointmentResponse book(Long patientId, Long slotId) {
    User patient = userRepository.findById(patientId)...;

    Slot slot = slotRepository.findByIdForUpdate(slotId)...;   // (1) LOCK the row

    if (slot.isBooked()) {                                      // (2) already taken?
        throw new ApiException(HttpStatus.CONFLICT, "Sorry, this slot was just booked...");
    }

    slot.setBooked(true);                                       // (3) mark as taken
    slotRepository.save(slot);
    Appointment appointment = appointmentRepository.save(new Appointment(patient, slot)); // (4)
    return toResponse(appointment);
}
```
Why the lock (1)? Read [06-WORKFLOWS.md](06-WORKFLOWS.md), section "Double-booking protection".

#### What is `@Transactional`?
A **transaction** = a group of database steps that succeed **together or fail together**.
**Bank example:** Transfer Rs. 500 = (take 500 from A) + (add 500 to B). If the second step fails, the first must be undone. Transaction guarantees this.

---

## 6. controller/ - the front desk

```java
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @GetMapping("/{id}/slots")
    public List<SlotResponse> getSlots(@PathVariable Long id) {
        return doctorService.getAvailableSlots(id);
    }
}
```

| Annotation | Meaning |
|-----------|---------|
| `@RestController` | This class answers web requests and returns JSON |
| `@RequestMapping("/api/doctors")` | Base URL for all methods inside |
| `@GetMapping("/{id}/slots")` | Answers `GET /api/doctors/5/slots` |
| `@PostMapping`, `@PutMapping` | POST (create), PUT (update) |
| `@PathVariable Long id` | Takes the `5` from the URL |
| `@RequestParam String specialization` | Takes `?specialization=card` from the URL |
| `@RequestBody BookRequest body` | Takes the JSON body |

A controller method has **no logic**. It only calls a service.

Five controllers:

| Controller | URL prefix | Who can use |
|------------|-----------|-------------|
| AuthController | `/api/auth` | Everyone |
| DoctorController | `/api/doctors` | Any logged-in user |
| AppointmentController | `/api/appointments` | PATIENT |
| DoctorPanelController | `/api/doctor` | DOCTOR |
| AdminController | `/api/admin` | ADMIN |

**How does a controller know who is calling?** `AuthInterceptor` read the token and stored the user's id on the request. The controller reads it:
```java
Long userId = (Long) request.getAttribute("userId");
```
Important: we take the user id **from the token**, never from the request body. Otherwise a patient could pretend to be someone else.

---

## 7. security/ - JWT and roles

### JwtUtil.java
- `generateToken(user)`: builds a token containing user id + role + expiry, signed with our secret key.
- `parse(token)`: checks the signature and expiry. Throws an exception if fake or expired.

A real token looks like this (3 parts separated by dots):
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwicm9sZSI6IlBBVElFTlQiLC4uLn0.k3j2...
   header                       payload (id, role, expiry)           signature
```
The payload is only **encoded**, not secret. Anyone can read it. So never put passwords in a token. The **signature** proves nobody changed it.

### AuthInterceptor.java - the security guard
Runs **before every `/api/**` request**:

```
Request arrives
   │
   ├─ path starts with /api/auth/ ?  -> let in (login/register are public)
   │
   ├─ header "Authorization: Bearer <token>" missing?  -> 401 "Please login first"
   │
   ├─ token fake or expired?                            -> 401 "Invalid or expired token"
   │
   ├─ path /api/admin/**  and role != ADMIN   -> 403
   ├─ path /api/doctor/** and role != DOCTOR  -> 403
   ├─ path /api/appointments and role != PATIENT -> 403
   │
   └─ OK -> save userId + role on the request -> go to the controller
```

**401 vs 403:** 401 = "I do not know who you are" (no/invalid wristband). 403 = "I know who you are, but you are not allowed here" (a patient trying the admin room).

---

## 8. exception/ - clean error messages

Anywhere in the code we can write:
```java
throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
```
`GlobalExceptionHandler` catches it and sends React:
```json
{ "error": "This email is already registered" }
```
with HTTP status 409. React shows the text in a red box.

Common status codes used:

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | Normal success |
| 400 | Bad request | Missing field, password too short |
| 401 | Not logged in | No/expired token, wrong password |
| 403 | Forbidden | Wrong role, not your appointment |
| 404 | Not found | Doctor/slot does not exist |
| 409 | Conflict | Email exists, slot already booked |

---

## 9. config/

### WebConfig.java
1. Registers `AuthInterceptor` for `/api/**`
2. **CORS** settings: browsers block a page from `localhost:5173` calling `localhost:8080` unless the backend says "I allow it". (We also use a proxy in Vite, so you will not notice CORS in development.)
3. Creates the `PasswordEncoder` (BCrypt) that other classes receive automatically.

### DataLoader.java
Runs once at startup (`CommandLineRunner`). If the `users` table is empty it inserts the demo admin, patient, 3 doctors and slots for the next 3 days. This is why you can login immediately.

---

## application.properties explained

```properties
server.port=8080                         # backend URL port
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db?...   # where the DB is
spring.datasource.username=${DB_USER:root}       # ${NAME:default} = env variable or default
spring.jpa.hibernate.ddl-auto=update     # create/alter tables automatically
spring.jpa.show-sql=true                 # print every SQL in the console
app.jwt.secret=...                       # secret used to sign tokens (min 32 chars)
app.jwt.expiration-ms=86400000           # 24 hours = 24*60*60*1000 ms
```

`ddl-auto` options: `update` (keep data, add what is missing), `create-drop` (fresh tables every run, used by H2), `validate`, `none`.

---

## Try this: follow one request through the code

`GET /api/doctors?specialization=card` (as a logged-in patient):

1. `AuthInterceptor.preHandle` -> token OK, role PATIENT, path `/api/doctors` has no special rule -> pass.
2. `DoctorController.search("card")`
3. `DoctorService.searchDoctors("card")`
4. `DoctorRepository.findBySpecializationContainingIgnoreCase("card")` -> SQL runs (look at your console, `show-sql=true`!)
5. Each `Doctor` is converted into a `DoctorResponse`
6. Jackson converts the list into JSON -> React.

Next: [05-FRONTEND-TUTORIAL.md](05-FRONTEND-TUTORIAL.md)
