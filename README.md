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
database/                reference schema + seed SQL (optional)
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

Backend (see `backend/.env.example`):
- `PORT` (default `8080`)
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `FRONTEND_ORIGIN` (CORS, default `http://localhost:5173`)
- `SEED_DEMO` (default `true`)

Frontend (see `frontend/.env.example`):
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

CI (`.github/workflows/ci.yml`) runs the backend tests and the frontend build.

## Deployment notes

- Backend: deploy `backend/` to a Java host (e.g. Render) with `DB_URL`, `DB_USER`,
  `DB_PASSWORD`, `FRONTEND_ORIGIN`, and `SEED_DEMO=false`.
- Frontend: deploy `frontend/` to Vercel (`npm run build`, output `dist`). Set
  `VITE_API_BASE` to the backend URL, or proxy `/api` to the backend.
- Host MySQL separately (a managed MySQL provider). The app only needs a database
  URL and credentials.
