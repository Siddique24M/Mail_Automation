@echo off

REM ============================================================
REM  Choose DB mode:
REM    Supabase (default): set USE_LOCAL_DB=false
REM    Local pgAdmin:      set USE_LOCAL_DB=true
REM ============================================================
set USE_LOCAL_DB=true

echo Starting Backend Server...
if "%USE_LOCAL_DB%"=="true" (
    echo [MODE] Using LOCAL pgAdmin database ^(mail_automation^)
    start "Backend" cmd /c "cd backend && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local"
) else (
    echo [MODE] Using SUPABASE database
    start "Backend" cmd /c "cd backend && mvnw.cmd spring-boot:run"
)

echo Starting Frontend Server...
start "Frontend" cmd /c "cd frontend && npm run dev"

echo Waiting 6 seconds for servers to initialize...
timeout /t 6 /nobreak > NUL

echo Opening browser...
start http://localhost:5173

echo All processes started successfully!
