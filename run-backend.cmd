@echo off
cd /d "%~dp0backend"
if not defined DB_USER set "DB_USER=root"
echo Starting backend at http://localhost:8080 ...
echo (If the DB password is not set, run: set "DB_PASSWORD=yourpassword")
call mvn spring-boot:run
