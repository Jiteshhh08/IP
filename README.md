# Student Complaint Management System

One real application. Students submit complaints with password login, admins resolve them. **MySQL is the only source of truth** — no demo data in the browser, no second app for viva.

## Features

- Student: register, login (ID or email + password), dashboard with stats, submit complaint, own complaints only, detail + admin response, profile, logout.
- Admin: login, dashboard with totals + search (ID/student/title) + status/category filters, complaint detail, update status + remark.
- Lifecycle: `Pending → In Progress → Resolved` (or `Rejected`). Student sees updates straight from the DB.

## Tech Stack

HTML + CSS + vanilla JS. Java (JDK `com.sun.net.httpserver.HttpServer`, JDBC) + MySQL. No Spring, no Hibernate, no React, no Tomcat. Passwords: PBKDF2-HMAC-SHA256 (JDK only).

## Architecture

```
Browser (fetch, cookie SESSION_ID)
   ↓
Main.java  →  StaticHandler (pages + static files + role guard)
              LoginHandler / RegisterHandler / LogoutHandler
              ComplaintHandler (student API) / AdminHandler (admin API)
   ↓
AuthService / ComplaintService (validation)
   ↓
UserDAO / ComplaintDAO (PreparedStatement only)
   ↓
MySQL (students, admin, complaints)
```

## Database

- `database/schema.sql` creates `student_complaint_system`:
  - `students(student_id PK, name NOT NULL, email UNIQUE NOT NULL, password_hash NOT NULL, created_at)`
  - `admin(username PK, password_hash NOT NULL)`
  - `complaints(complaint_id AUTO PK, student_id FK → students ON DELETE CASCADE, title, category, description, status DEFAULT 'Pending', admin_remark, created_at, updated_at, INDEX(student_id), INDEX(status), CHECK status IN (...))`
- `database/seed.sql` inserts demo rows with real PBKDF2 hashes (optional — the server auto-seeds empty tables on startup via `DbInit`).

## Setup

1. Install JDK 8+ and MySQL. The JDBC driver is already at `lib/mysql-connector-j.jar`.
2. Create + seed schema (optional — auto-created on first start):
   ```powershell
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/seed.sql
   ```
3. Configure (no secrets in code):
   - `DB_URL` (default `jdbc:mysql://localhost:3306/student_complaint_system?...`)
   - `DB_USER` (default `root`)
   - `DB_PASSWORD` (default empty)
   - Windows: `set DB_PASSWORD=secret`
4. Compile + run (from project root, PowerShell):
   ```powershell
   $files = (Get-ChildItem -Recurse -Filter *.java src).FullName
   javac -d out -cp "lib/mysql-connector-j.jar" $files
   java -cp "out;lib/mysql-connector-j.jar" Main
   ```
   Mac/Linux: replace `;` with `:`.
5. Open `http://localhost:8080`.

## Admin Setup

Initial admin is created by `DbInit.seed()` on first start: `admin / admin123`. Change it after first login by updating the DB (hash via `java PasswordUtil <newpass>`), or extend the app with change-password. Students self-register; seed students `STU001–STU004 / pass123` exist for demo.

## Flows

Student: `index → register/login → /student/dashboard (stats + recent) → /student/complaints/new → /student/complaints → /student/complaints/{id} (status + remark)`.
Admin: `/admin/login → /admin/dashboard (stats + search/filter) → /admin/complaints/{id} → update → student sees change`.

## Security

- PBKDF2 hashes (`password_hash`), constant-time verify; no plaintext anywhere in DB.
- Server-side sessions (UUID cookie `SESSION_ID`, HttpOnly, 8h expiry). IDs come from session, never browser `student_id`.
- RBAC on backend: `/api/student/*` = STUDENT, `/api/admin/*` = ADMIN; static pages redirect (unauthenticated → login, wrong role → own dashboard).
- Ownership: `GET /api/student/complaints/{id}` returns 403 for another student's complaint.
- All SQL via `PreparedStatement`; status/category allowlisted; search uses `LIKE ?` params.
- Validation both sides; output escaped (`esc()` + JSON escape); generic 500s, details only in server log.

## Project Structure

```
src/
  Main.java                     # entry point + HTTP routes
  config/DatabaseConnection.java config/DbInit.java
  model/User.java               model/Complaint.java
  dao/UserDAO.java              dao/ComplaintDAO.java
  service/AuthService.java      service/ComplaintService.java
  handler/StaticHandler.java    handler/LoginHandler.java
  handler/RegisterHandler.java  handler/LogoutHandler.java
  handler/ComplaintHandler.java handler/AdminHandler.java
  util/Http.java  util/Json.java  util/PasswordUtil.java  util/SessionManager.java
database/schema.sql  database/seed.sql
frontend/index.html  login.html  register.html  admin-login.html
frontend/student-dashboard.html  complaints.html  submit-complaint.html
frontend/complaint-details.html  admin-dashboard.html  admin-complaint.html  profile.html
frontend/css/style.css  auth.css  dashboard.css
frontend/js/app.js  auth.js  student.js  admin.js
lib/mysql-connector-j.jar
```

## Routes

- Pages: `/`, `/login`, `/register`, `/admin/login`, `/student/dashboard`, `/student/complaints`, `/student/complaints/new`, `/student/complaints/{id}`, `/student/profile`, `/admin/dashboard`, `/admin/complaints/{id}`, `/admin/profile`
- API: `POST /api/login`, `GET /api/me`, `POST /api/register`, `POST /api/logout`,
  `GET /api/student/stats`, `GET|POST /api/student/complaints`, `GET /api/student/complaints/{id}`,
  `POST /api/admin/login`, `GET /api/admin/stats`, `GET /api/admin/complaints`, `GET|PUT /api/admin/complaints/{id}`

## Viva (5 min)

1. `seed.sql` → show 3 tables + hashes (not passwords).
2. Register student → `SELECT * FROM students` (hash, not plaintext).
3. Submit → `SELECT * FROM complaints` (Pending).
4. Admin login → search/filter → open → `In Progress` + remark → student page shows it (same DB row).
5. Point to `SessionManager` (cookie), `ComplaintDAO.findById` ownership check, `PasswordUtil.verify`.
