# 09 - Practice Exercises

The best way to understand a project is to **change it yourself**. Do these in order. Every exercise tells you which files to touch.

## Easy

### 1. Change the hospital name
`frontend/src/components/Navbar.jsx` -> change `City Hospital`. Save and watch the browser update.

### 2. Change the demo slot times
`backend/.../config/DataLoader.java` -> change the `starts` array (for example add `LocalTime.of(12, 0)`). Restart the backend (H2 resets the data).

### 3. Show the experience in a nicer way
`frontend/src/pages/Doctors.jsx` -> change `{doctor.experienceYears} years experience` to show "Senior doctor" when it is 10 or more (use a simple `if` or the `? :` operator).

### 4. Change the token validity to 1 hour
`application.properties` -> `app.jwt.expiration-ms=3600000`. Login, wait an hour (or set it to `60000` = 1 minute) and watch the app send you back to the login page.

## Medium

### 5. Add a phone number to the patient
1. `model/User.java` -> add `private String phone;` + getter.
2. `dto/Dto.java` -> add `String phone` to `RegisterRequest` (and `AuthResponse` if you want).
3. `AuthService.register` -> pass it into the `User`.
4. `frontend/src/pages/Register.jsx` -> add an input and send it.
5. If using MySQL, `ddl-auto=update` adds the new column automatically.

### 6. Add "reason for visit" to an appointment
Same idea: `Appointment.reason`, `BookRequest(slotId, reason)`, show it in the doctor's table.

### 7. Doctor can delete a free slot
- `SlotRepository` already has `deleteById`.
- `DoctorService.deleteSlot(userId, slotId)`: check the slot belongs to this doctor and `booked == false`, otherwise throw an `ApiException`.
- `DoctorPanelController`: `@DeleteMapping("/slots/{id}")`.
- `DoctorDashboard.jsx`: a **Delete** button on free slots (`api.delete("/doctor/slots/" + id)`).

### 8. Hide past slots on the doctor's dashboard
Change the query in `SlotRepository.findAllOfDoctor` to only return slots with `slotDate >= :today`.

## Harder

### 9. Add "my profile" page
`GET /api/me` returns name, email and role from the token's userId. Show it on a new React page.

### 10. Stop a patient from booking two slots at the same time
In `AppointmentService.book`, before saving, check if this patient already has a BOOKED appointment at the same date and start time. (Add a query to `AppointmentRepository`.)

### 11. Add validation messages
Add `spring-boot-starter-validation`, use `@NotBlank` / `@Email` on the request records and `@Valid` in controllers. Handle `MethodArgumentNotValidException` in `GlobalExceptionHandler`.

### 12. Write your first unit test
Add `spring-boot-starter-test`, then test `AuthService.register` with Mockito: when the email already exists it must throw `ApiException` with status 409.

### 13. Prove the double-booking protection
Write a test (or a small script) that fires 10 booking requests for the same slot in parallel using threads. Expect exactly **one** success and nine 409 responses.

### 14. Replace the interceptor with Spring Security
Once you are comfortable, learn Spring Security: create a `JwtAuthFilter` (`OncePerRequestFilter`) and a `SecurityFilterChain` bean with `requestMatchers("/api/admin/**").hasRole("ADMIN")`. Compare with `AuthInterceptor`. The ideas are the same.

## Make it a stronger resume project

- Push it to GitHub with a good README and screenshots (take screenshots of every page).
- Deploy the frontend (Netlify / Vercel) and backend (Render / Railway) and add the **Live Demo** link.
- Add a short "Features" and "How I prevent double booking" section to your GitHub README.
