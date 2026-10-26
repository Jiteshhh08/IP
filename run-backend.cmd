@echo off
setlocal
set "JAVA_HOME=C:\Users\Admin\AppData\Local\opencode-tools\jdks\jdk21"
set "MAVEN_HOME=C:\Users\Admin\AppData\Local\opencode-tools\maven\apache-maven-3.9.9"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
cd /d "%~dp0backend"

if not defined DB_USER set "DB_USER=root"
if not defined DB_PASSWORD set /p "DB_PASSWORD=Enter MySQL password for root: "

echo Starting backend at http://localhost:8080 ...
call mvn spring-boot:run
endlocal
