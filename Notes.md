# Project Notes - Student Complaint Management System

These notes explain the important pieces of the project, why they are used, how
they work, and how the app is deployed.

## 1. What the app does

- Students register or log in with a student ID or email.
- Students submit complaints, see their own complaints, and track the status.
- Admins log in separately, search/filter all complaints, and update status with
  a remark.
- Complaint status flow: `Pending -> In Progress -> Resolved`, or
  `Pending`/`In Progress -> Rejected`. `Resolved` and `Rejected` are final.

## 2. Stack and why it is used

| Layer | Technology | Why |
|---|---|---|
| Language | Java 21 | LTS version, required by Spring Boot 3.5, modern language features. |
| Backend framework | Spring Boot 3.5 | Auto-configuration, embedded Tomcat, one runnable jar. |
| REST API | Spring Web | Controllers, JSON request/response, no extra framework. |
| Persistence | Spring Data JPA + Hibernate | Entities and repositories, no boilerplate SQL. `ddl-auto: update` creates tables. |
| Validation | Bean Validation | Declarative rules on request records (`@NotBlank`, `@Email`, `@Size`). |
| Database | MySQL 8 | Relational data with a clear student/complaint relationship. |
| Frontend | React 18 | Component-based UI with a small amount of state. |
| Build/dev server | Vite 5 | Fast dev server, `/api` proxy, production build into `dist/`. |
| Routing | React Router 6 | Client-side routes and role-protected layouts. |
| Styling | Plain CSS | No UI framework needed; the app is small. |
| Auth | `HttpSession` + `JSESSIONID` cookie | Simple server-side session; no JWT library or token storage. |
| Password hashing | PBKDF2 (JDK `SecretKeyFactory`) | Salted, slow hashing; no plaintext passwords and no external dependency. |
| Deployment | Docker + Render + Aiven MySQL | One Docker image serves UI and API; managed MySQL with TLS. |

## 3. Backend - important parts and how they work

### Entry point and configuration

- `ComplaintApplication.java` starts Spring Boot.
- `application.yml`:
  - `server.port: ${PORT:8080}` so Render can inject its port.
  - Session cookie `same-site: lax`.
  - Datasource from `DB_URL`, `DB_USER`, `DB_PASSWORD`.
  - `spring.jpa.hibernate.ddl-auto: update` - Hibernate creates/updates tables.
  - `open-in-view: false` - database access stays inside services.
  - `app.cors-origin` from `FRONTEND_ORIGIN` (only needed for split deploys).
  - `app.seed-demo` from `SEED_DEMO` (default `true`).

### Domain model

- `Student`: `studentId` (primary key), `name`, unique `email`,
  `passwordHash` (hidden from JSON with `@JsonIgnore`), `createdAt`.
- `Admin`: `username` (primary key), `passwordHash` (hidden from JSON).
- `Complaint`: auto-increment `complaintId`, many-to-one `student`,
  `title`, `category`, `description`, `status`, `adminRemark`,
  `createdAt`, `updatedAt`.

### Authentication and authorization

- `AuthService` handles register, student login, and admin login.
  - Student IDs are uppercased; emails are lowercased.
  - Student login accepts an ID or an email (anything containing `@` is treated
    as email).
  - Admin login is a case-sensitive username lookup.
  - Invalid credentials return a generic `401` message.
- `PasswordUtil`:
  - Hash format: `pbkdf2:<iterations>:<saltBase64>:<hashBase64>`.
  - New hashes use 210,000 iterations and a 256-bit key.
  - Verification reads the iteration count from the stored hash, so old
    65,536-iteration demo hashes still log in.
  - Uses `MessageDigest.isEqual` for constant-time comparison.
- `AuthInterceptor` guards `/api/student/**` and `/api/admin/**`:
  - No session -> `401`.
  - Wrong role -> `403` (student cannot call admin endpoints and vice versa).
  - `/api/auth/**` is public.
- On successful login, a `SessionUser` (`id`, `name`, `email`, `role`) is stored
  in the `HttpSession`.

### Controllers

- `AuthController` (`/api/auth`): register, login, admin login, logout, `me`.
- `StudentController` (`/api/student`): dashboard, own complaints, create,
  detail (ownership checked), profile.
- `AdminController` (`/api/admin`): dashboard, all complaints with
  `search`/`status`/`category` filters, detail, status update, profile.

### Business rules

- `ComplaintService`:
  - Allowed categories: `Academic`, `Infrastructure`, `Hostel`, `Library`,
    `Canteen`, `Other`.
  - Status transitions are enforced; invalid transitions return `409`.
  - Students can only read their own complaint detail; otherwise `403`.
  - Dashboards return `stats` and the 5 most recent complaints.
  - Search matches complaint ID, student ID, student name, or title.

### Validation and errors

- `Requests.java` defines validated records:
  - Student ID: 3-20 letters/digits.
  - Name: max 100 characters.
  - Email: valid email.
  - Password: 6-100 characters.
  - Complaint title: max 150; description: max 5,000; remark: max 2,000.
- `ApiException` + `ApiExceptionHandler`:
  - Business errors -> `{ "error": "..." }` with the correct status.
  - Validation errors -> `400` with an `error` and a `fields` map.
  - Unexpected errors -> `500` with a generic message (details only in logs).

## 4. Database

Tables are created by Hibernate on startup:

- `students`: `student_id`, `name`, `email`, `password_hash`, `created_at`.
- `admin`: `username`, `password_hash`.
- `complaints`: `complaint_id`, `student_id`, `title`, `category`,
  `description`, `status`, `admin_remark`, `created_at`, `updated_at`.

`DemoDataSeeder` runs on startup when `SEED_DEMO=true`:

- Creates `admin` / `admin123` only if the `admin` table is empty.
- Creates `STU001`-`STU004` / `pass123` only if `students` is empty.
- Creates 6 example complaints only if `complaints` is empty.

Because each step checks the table count, the seeder is idempotent and will not
duplicate data on every restart.

Connection settings:

- `DB_URL` - JDBC URL, for example:
  `jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- `DB_USER`, `DB_PASSWORD` - separate variables, never inside the URL.

## 5. Frontend - important parts and how they work

- `main.jsx` mounts React inside `BrowserRouter` and `AuthProvider`.
- `AuthContext.jsx`:
  - On load, calls `GET /api/auth/me` to restore the session.
  - `login`, `adminLogin`, `register`, and `logout` update the current user.
- `api.js`:
  - Uses relative `/api` paths.
  - Sends `credentials: "include"` so the session cookie is used.
  - Throws an `Error` with the backend's message on failure.
- `App.jsx`:
  - Routes for student and admin pages.
  - `Protected` checks `loading`, `user`, and `role`; redirects to the correct
    login page or dashboard.
- `Layout.jsx`: sidebar per role, current user, and logout.
- `vite.config.js`:
  - Dev server on port `5173`.
  - Proxies `/api` to `http://localhost:8080`, so the browser sees same-origin
    requests during development.

## 6. Local development

Requirements: JDK 21, Maven 3.9+, Node 18+, MySQL 8.

Backend:

```powershell
cd backend
$env:DB_USER="root"
$env:DB_PASSWORD="your-password"
mvn spring-boot:run
```

Or on Windows, run `run-backend.cmd` (it sets Java/Maven paths and prompts for the
MySQL password if `DB_PASSWORD` is not set).

Frontend:

```powershell
cd frontend
npm install
npm run dev
```

Or run `run-frontend.cmd`. Open http://localhost:5173.

Demo logins:

- Admin: `admin` / `admin123` (use `/admin/login`, lowercase `admin`)
- Students: `STU001`-`STU004` / `pass123`

Tests and production build:

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

## 7. Deployment

### Why one service

The `Dockerfile` builds the React app and copies `frontend/dist` into the Spring
Boot jar's `src/main/resources/static`. The result is one image and one public URL
that serves:

- the React app at `/`
- the API at `/api/**`

Because the browser sees one origin, there is no CORS setup and session cookies
work normally. No separate Vercel/Netlify deployment is needed.

### Dockerfile stages

1. `node:20-alpine` - runs `npm ci` and `npm run build` in `frontend/`.
2. `maven:3.9-eclipse-temurin-21` - builds the backend and copies the frontend
   `dist` folder into the backend's static resources, then packages the jar.
3. `eclipse-temurin:21-jre` - runs the jar with `java -jar app.jar`.

`.dockerignore` keeps `.git`, `target`, `node_modules`, and `dist` out of the
build context.

### SPA fallback

`WebConfig` adds a resource handler that serves `static/index.html` for non-API
paths. This makes React Router deep links such as `/admin/login` work on refresh.
Paths starting with `api/` and missing files with an extension are not redirected.

### Deployed setup

- App: Render Web Service (Docker, Free plan)
- Database: Aiven MySQL (Free plan, TLS required)
- Live URL: https://ip-xpuo.onrender.com

Render settings:

- Repository: `Jiteshhh08/IP`
- Branch: `feat/modernization`
- Runtime: Docker
- Root Directory: empty (repo root)
- Dockerfile Path: `Dockerfile`
- Build/Start commands: none (the Dockerfile handles both)
- Health check path: `/`

Environment variables on Render:

```text
DB_URL=jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=your-db-user
DB_PASSWORD=your-db-password
JAVA_TOOL_OPTIONS=-Xmx320m
```

`SEED_DEMO` defaults to `true`; `PORT` is injected by Render and read by
`application.yml`. Do not put the password inside `DB_URL`.

Aiven notes:

- Use the host, port, database (`defaultdb`), user (`avnadmin`), and password
  from the Aiven connection page.
- SSL mode must be `REQUIRED`.
- Add `0.0.0.0/0` to the allowed IP addresses because Render's outbound IPs are
  dynamic.
- Copy the password with the copy icon (not from the Service URI, which can be
  URL-encoded).
- Confirm the service is on the Free plan if you do not want trial credits used.

### Free-tier behavior

- Render free instances sleep after about 15 minutes of inactivity.
- The first request after sleeping can take 30-60 seconds.
- Sessions are in memory, so a restart or redeploy logs everyone out; keep one
  instance. Data itself is stored in MySQL and survives restarts.

### Verifying a deployment

```powershell
curl.exe -i https://YOUR-APP.onrender.com/
curl.exe -i https://YOUR-APP.onrender.com/admin/login
curl.exe -i https://YOUR-APP.onrender.com/api/auth/me
curl.exe -i -X POST https://YOUR-APP.onrender.com/api/auth/admin/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Expected: the first two return `200` HTML, `/api/auth/me` returns `401` without a
session, and admin login returns `200` with the admin JSON.

### Troubleshooting

- `Access denied` from MySQL: copy the password again from Aiven and update
  `DB_PASSWORD`; also check the allowed IP addresses.
- Admin login fails: use `/admin/login` and the lowercase username `admin`.
- `502`/timeout: the free instance is waking up; wait and retry.
- SPA route 404s: confirm the Docker image was rebuilt and the SPA fallback in
  `WebConfig` is present.
- Build fails looking for `frontend/`: leave Render's Root Directory empty.

## 8. Security notes

- No secrets are committed: database credentials are Render environment variables.
- The demo accounts (`admin123`, `pass123`) are created by the seeder for the
  college demo. Change them before sharing the URL publicly.
- If a database password has been exposed (for example in a screenshot), rotate it
  in Aiven, update `DB_PASSWORD` on Render, and redeploy.
- Passwords are stored only as salted PBKDF2 hashes.
- The session cookie is `HttpOnly` by default, which keeps it out of JavaScript.

## 9. Known limitations

- No password-change or admin-user management UI; the seeded admin is fixed.
- No delete complaint feature.
- Sessions are server-side and in memory; horizontal scaling would need sticky
  sessions or a shared session store.
- Filtering is done in Java after loading complaints, which is fine for a demo
  but would be moved to database queries for large datasets.
