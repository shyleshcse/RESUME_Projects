# 08 - Interview Questions About This Project

Your resume says this project uses React, Spring Boot, MySQL, JWT, role-based access, double-booking prevention and Postman. An interviewer **will** ask about each of these. Practise saying the answers in your own words.

## 30-second project introduction (say this first)

> "It is a Hospital Appointment Booking System. Patients can search doctors by specialization, see free slots and book online. Doctors manage their slots and mark visits completed, and an admin adds doctors. The frontend is React, the backend is Spring Boot REST APIs with Spring Data JPA and MySQL. Login uses JWT with three roles, and I prevent double booking by locking the slot row inside a transaction. I tested all APIs with Postman."

---

## A. Project and architecture

**Q1. Explain the architecture.**
Three parts: React frontend, Spring Boot backend, MySQL database. React calls REST APIs with axios, the backend uses controller -> service -> repository layers, and the repository talks to MySQL through JPA.

**Q2. Why controller, service and repository layers?**
Each class has one job. Controller handles HTTP only, service holds business rules, repository handles the database. It is easier to read, test and change.

**Q3. What is a REST API?**
A way to expose data over HTTP using URLs and methods: GET to read, POST to create, PUT to update, DELETE to remove. Data is exchanged as JSON. Example: `GET /api/doctors`.

**Q4. What is a DTO and why use it?**
A Data Transfer Object is a plain class that carries only the data needed for a request or response. I use it so the password hash in the `User` entity is never sent to the frontend, and so the JSON shape is independent of the table structure.

**Q5. What is dependency injection?**
Spring creates objects and passes them into constructors. For example `AppointmentService` receives its repositories in the constructor, so I never write `new`. It makes code loosely coupled.

---

## B. JWT and security

**Q6. How does login work in your project?**
The user sends email and password. The service finds the user, compares the password with the stored BCrypt hash, and if it matches creates a JWT containing the user id and role. React stores the token and sends it in the `Authorization: Bearer` header on every request.

**Q7. What is a JWT?**
A signed token with three parts: header, payload (user id, role, expiry) and signature. The server signs it with a secret key, so it can detect if someone changes it. It is stateless: the server does not store sessions.

**Q8. Is the JWT payload secret?**
No. It is only Base64 encoded, so anyone can read it. The signature only prevents modification. So I never store passwords in it.

**Q9. How did you implement role-based access?**
The role is stored inside the token. A `HandlerInterceptor` runs before every `/api/**` request, validates the token, and checks the role against the URL: `/api/admin/**` needs ADMIN, `/api/doctor/**` needs DOCTOR, `/api/appointments/**` needs PATIENT. Wrong role gives 403.

**Q10. 401 vs 403?**
401 Unauthorized = not authenticated (no or invalid token). 403 Forbidden = authenticated but not allowed.

**Q11. How are passwords stored?**
Hashed with BCrypt, which adds a random salt and is slow on purpose to resist brute force. I compare using `passwordEncoder.matches()`.

**Q12. Why did you use an interceptor and not Spring Security?**
To keep the project simple and understand every step of authentication. In a production project I would use Spring Security with a JWT filter, method security (`@PreAuthorize`) and CSRF/CORS configuration. The concepts are the same: validate the token, build the user identity, check the role.

**Q13. How does the backend know which user is calling?**
The interceptor reads the user id from the token and puts it on the request. Controllers use that id. I never trust a user id sent in the request body, otherwise a patient could book as someone else.

**Q14. Where do you store the token in React? Any risk?**
In `localStorage`. The risk is XSS: injected JavaScript can read it. A more secure option is an HttpOnly cookie. For a learning project localStorage is common.

**Q15. What is CORS?**
Browsers block a page from one origin (`localhost:5173`) calling another origin (`localhost:8080`) unless the server allows it. I configured allowed origins in `WebConfig`, and in development I also used the Vite proxy.

---

## C. Database and JPA

**Q16. Explain your tables and relationships.**
`users`, `doctors`, `slots`, `appointments`. A doctor is one-to-one with a user. A doctor has many slots (one-to-many). A patient has many appointments, and each appointment points to one slot.

**Q17. Primary key vs foreign key?**
Primary key uniquely identifies a row (`id`). Foreign key is a column that references the primary key of another table (`slots.doctor_id` -> `doctors.id`).

**Q18. What is JPA and Hibernate?**
JPA is the standard (a specification) for mapping Java objects to tables. Hibernate is the implementation Spring Boot uses by default. Spring Data JPA adds repositories that generate queries from method names.

**Q19. What does `ddl-auto=update` do?**
Hibernate creates missing tables/columns at startup without deleting data. For production I would use migration tools like Flyway and set it to `validate`.

**Q20. What are `@OneToOne` and `@ManyToOne`?**
Relationship annotations. `@ManyToOne` on `Slot.doctor` means many slots belong to one doctor and creates a `doctor_id` foreign key column.

**Q21. What does `findByEmail` do without any SQL?**
Spring Data JPA parses the method name and generates `select ... where email = ?`.

**Q22. What is `@Transactional`?**
It runs the method in one database transaction: all operations commit together or roll back together. In `addDoctor` both the user and doctor rows are saved, or neither.

**Q23. Why do you not delete a cancelled appointment?**
To keep history. I change `status` to CANCELLED and free the slot.

**Q24. Which SQL would you write to get appointments with doctor name?**
A join of `appointments`, `slots`, `doctors` and `users` (see `docs/03-DATABASE.md`).

---

## D. Double booking (they love this one)

**Q25. How did you prevent double booking?**
Inside a `@Transactional` method I load the slot with a pessimistic write lock (`SELECT ... FOR UPDATE`). If two patients book at the same time, the second request waits until the first commits, then sees `booked = true` and receives a 409 Conflict.

**Q26. What is pessimistic vs optimistic locking?**
Pessimistic: lock the row first, assume conflicts will happen. Optimistic: do not lock, add a `@Version` column and fail on commit if someone else changed the row, then retry. Pessimistic is simple and safe for a hot resource like one slot.

**Q27. Why is checking `booked` without a lock not enough?**
Both requests can read `false` at the same moment, then both save `true`. The lock makes the check and the update one atomic step.

**Q28. What else could protect it at the database level?**
A unique constraint on (slot_id) for active appointments. I did not use a plain unique on `slot_id` because cancelled rows keep the same slot id.

---

## E. React

**Q29. What are hooks you used?**
`useState` for data that changes the screen, `useEffect` to load data when a page opens, `useParams` for the id in the URL, `useNavigate` to redirect.

**Q30. How do protected routes work?**
`ProtectedRoute` checks the user and role and redirects. It is only user-experience. Real security is on the backend.

**Q31. How does the token get added to requests?**
An axios request interceptor reads it from localStorage and sets the Authorization header for every call.

**Q32. Why is `key` needed in `map()`?**
It helps React identify each list item so it updates efficiently.

**Q33. What does `event.preventDefault()` do in a form?**
Stops the browser from reloading the page on submit so React can handle it.

---

## F. Testing, tools and improvements

**Q34. How did you test the APIs?**
With Postman: a collection with login requests that save the token in a variable, then requests for each role, including negative cases (wrong role gives 403, double booking gives 409).

**Q35. What would you improve next?**
Spring Security + refresh tokens, input validation with `@Valid`, pagination, email notifications, unit tests with JUnit/Mockito, Docker, and deployment.

**Q36. What was the hardest part?**
(Suggested answer) Making sure two patients cannot take the same slot. I learned about transactions and row locking to solve it.

**Q37. What would change if 10,000 patients used it at once?**
Add database indexes (doctor_id, slot_date), pagination, connection pool tuning, caching of the doctor list, and run multiple backend instances behind a load balancer. Row locks still work because the lock is in the database.

---

## Be honest in the interview

Know **why** every part exists. If you are asked something you do not know, say: "I have not used that yet, but my understanding is ... and I would learn it." That is far better than guessing.
