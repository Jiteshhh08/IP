# Student Complaint Management System

A simple full-stack app where students submit complaints and admins resolve them.
Students register and log in, submit and track complaints, and see admin responses.
Admins search and filter complaints, update status, and leave a remark.

## Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Bean Validation, MySQL.
- **Frontend:** React 18, Vite, React Router, plain CSS.
- **Auth:** session cookie (`JSESSIONID`), PBKDF2 password hashing.
- **Database:** MySQL `student_complaint_system` (`students`, `admin`, `complaints`).

## Project structure

```
backend/                 Spring Boot API
  src/main/java/com/college/complaints/
    config/              CORS/interceptor config, demo seeder
    domain/              Student, Admin, Complaint
    repository/          Spring Data repositories
    service/             AuthService, ComplaintService
    util/                PasswordUtil (PBKDF2)
    web/                 controllers, requests, interceptor, error handling
  src/main/resources/application.yml
frontend/                React + Vite app
  src/pages/             login, register, dashboards, complaints
  src/components/        layout
  src/api.js             fetch wrapper
```

## Database

Tables are created/updated automatically by Hibernate (`ddl-auto: update`).
The database and demo data are seeded on startup when the tables are empty
(`SEED_DEMO=true`, default). Set `SEED_DEMO=false` in production.

Default local logins:
- Admin: `admin` / `admin123`
- Students: `STU001`–`STU004` / `pass123`

Statuses: `Pending → In Progress → Resolved`, and `Pending`/`In Progress` may be
`Rejected`. Invalid transitions are rejected by the backend.

## Run locally

Backend (port 8080):

```powershell
cd backend
$env:DB_USER="root"; $env:DB_PASSWORD="your-password"
mvn spring-boot:run
```

Frontend (port 5173, proxies `/api` to the backend):

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.

### Environment variables

Backend:
- `PORT` (default `8080`)
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `FRONTEND_ORIGIN` (CORS, default `http://localhost:5173`)
- `SEED_DEMO` (default `true`)

Frontend:
- `VITE_API_BASE` (leave empty with the Vite proxy)

## API

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/admin/login` | public |
| POST | `/api/auth/logout` | public |
| GET | `/api/auth/me` | session |
| GET | `/api/student/dashboard` | student |
| GET | `/api/student/complaints` | student |
| POST | `/api/student/complaints` | student |
| GET | `/api/student/complaints/{id}` | student (owner) |
| GET | `/api/student/profile` | student |
| GET | `/api/admin/dashboard` | admin |
| GET | `/api/admin/complaints?search=&status=&category=` | admin |
| GET | `/api/admin/complaints/{id}` | admin |
| PATCH | `/api/admin/complaints/{id}` | admin |
| GET | `/api/admin/profile` | admin |

## Tests and build

```powershell
cd backend
mvn test            # unit tests

cd ../frontend
npm run build       # production build
```

## Deploy (single free service)

The included `Dockerfile` builds the React app and bundles it into the Spring Boot
jar, so one URL serves both the UI and the API (no CORS or session-cookie issues).

1. Create a free MySQL database (e.g. Aiven MySQL or TiDB Cloud Serverless) and note
   the host, port, user, password, and database name.
2. Push this repo, then on [Render](https://render.com) choose **New → Web Service**,
   connect the repo, and select **Docker** (Free plan).
3. Add environment variables:
   - `DB_URL=jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED&serverTimezone=UTC`
   - `DB_USER`, `DB_PASSWORD`
   - `SEED_DEMO=true` to create the demo admin/students on the first boot
   - `JAVA_TOOL_OPTIONS=-Xmx320m`
4. Deploy and open the Render URL. Free instances sleep after 15 minutes, so the
   first request may take 30-60 seconds.

Change the seeded `admin`/`admin123` password before sharing the URL publicly.
