# 02 - Setup and Run (Step by Step)

## Step 0 - Install these tools (one time)

| Tool | Version | Check command | Download |
|------|---------|---------------|----------|
| JDK (Java) | 17 or newer | `java -version` | https://adoptium.net |
| Maven | 3.8+ | `mvn -v` | https://maven.apache.org/download.cgi |
| Node.js | 18 or newer | `node -v` and `npm -v` | https://nodejs.org |
| MySQL (optional) | 8.x | `mysql --version` | https://dev.mysql.com/downloads/installer/ |
| Postman (optional) | any | | https://www.postman.com/downloads/ |

> **Maven not installed?** If you use **Eclipse** or **IntelliJ**, you do not need to install Maven separately. They have it built in. See "Run from Eclipse" below.

---

## Option A - Run WITHOUT MySQL (easiest, start here)

Uses a small in-memory database called **H2**. Data is erased when you stop the app. Perfect for learning.

### A1. Start the backend
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
The first time, Maven downloads libraries (2 to 5 minutes). When it is ready you will see:

```
==================== DEMO LOGINS ====================
  ADMIN   : admin@hospital.com   / admin123
  DOCTOR  : priya@hospital.com   / doctor123
  PATIENT : patient@test.com     / patient123
=====================================================
... Started HospitalApplication in 6.3 seconds
```
(Exact numbers and extra log lines will differ.)

### A2. Check the backend is alive
Open in the browser: http://localhost:8080/api/doctors
You should see `{"error":"Please login first"}`. **That is correct!** It means the security guard is working.

### A3. Start the frontend (a second terminal)
```bash
cd frontend
npm install
npm run dev
```
You will see `Local: http://localhost:5173/`. Open it.

### A4. Use the app
Login as `patient@test.com / patient123`. Follow the story in [06-WORKFLOWS.md](06-WORKFLOWS.md).

### Look inside the H2 database (optional)
While the backend is running open http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:hospital_db`
- User: `sa`, Password: (empty) -> Connect
- Run `SELECT * FROM users;`

---

## Option B - Run WITH MySQL (matches your resume)

### B1. Make sure MySQL is running and you know the root password.

### B2. Create the database (optional, the app can also create it)
```sql
CREATE DATABASE hospital_db;
```
The setting `createDatabaseIfNotExist=true` already does this automatically, so you can skip it.

### B3. Set your MySQL password
Open `backend/src/main/resources/application.properties` and look at:
```
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:root}
```
Either change the word after the colon (`root`) to your real password, or set an environment variable:

- Windows (CMD): `set DB_PASSWORD=yourpassword`
- Windows (PowerShell): `$env:DB_PASSWORD="yourpassword"`
- Mac/Linux: `export DB_PASSWORD=yourpassword`

### B4. Start the backend (without the h2 profile)
```bash
cd backend
mvn spring-boot:run
```
Spring creates the 4 tables automatically (`ddl-auto=update`) and the demo data is inserted on the first start.

### B5. See the data in MySQL
```sql
USE hospital_db;
SHOW TABLES;
SELECT * FROM users;
SELECT * FROM slots LIMIT 5;
```

The frontend steps are the same as Option A (A3, A4).

---

## Run from Eclipse / STS (no Maven install needed)

1. **File -> Import -> Maven -> Existing Maven Projects** -> choose the `backend` folder -> Finish. Wait for the dependencies to download (bottom-right progress bar).
2. Open `HospitalApplication.java` -> right-click -> **Run As -> Java Application**.
3. To use H2: **Run -> Run Configurations -> Java Application -> HospitalApplication -> Arguments tab -> Program arguments:** `--spring.profiles.active=h2` -> Run.

## Run from IntelliJ IDEA

1. **File -> Open** -> select `backend/pom.xml` -> Open as Project.
2. Open `HospitalApplication.java` and click the green Run arrow.
3. For H2: **Run -> Edit Configurations -> Active profiles:** `h2`.

## Run from VS Code

Install the "Extension Pack for Java" and "Spring Boot Extension Pack". Open the `backend` folder, then run `HospitalApplication`.

---

## Build a runnable JAR (optional)
```bash
cd backend
mvn clean package
java -jar target/hospital-backend-1.0.0.jar --spring.profiles.active=h2
```

---

## Troubleshooting

| Problem | Reason | Fix |
|---------|--------|-----|
| `Port 8080 was already in use` | Another program uses 8080 | Close it, or add `server.port=8081` in application.properties (then change the proxy in `frontend/vite.config.js` to 8081 too) |
| `Access denied for user 'root'@'localhost'` | Wrong MySQL password | Fix step B3 |
| `Communications link failure` | MySQL is not running | Start the MySQL service |
| `Unable to determine Dialect` | Cannot connect to the DB | Same as above, or use the H2 profile |
| `release version 17 not supported` | Your JDK is older than 17 | Install JDK 17+ and check `java -version` and `mvn -v` show the same Java |
| `mvn` is not recognized | Maven not installed or not in PATH | Install Maven or use Eclipse/IntelliJ |
| Frontend shows "Cannot reach the server" | Backend is not running | Start the backend first and check http://localhost:8080 |
| White blank page in the browser | JavaScript error | Press F12 -> Console tab and read the red error |
| `npm install` fails | Old Node | Install Node 18+ |
| Login says "Wrong email or password" with demo data | You are using MySQL and old data exists without demo users | Drop the database (`DROP DATABASE hospital_db;`) and start again |
| Dates of demo slots are in the past | MySQL data was created days ago | Login as a doctor and add new slots, or recreate the database |
| 401 on every call after a long time | The token expired (24 hours) | Login again |

## Stopping the project
Press **Ctrl + C** in each terminal.

Next: [03-DATABASE.md](03-DATABASE.md)
