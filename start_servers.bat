@echo off
echo Starting Backend Server...
start "Backend" cmd /c "cd backend && mvnw.cmd spring-boot:run"

echo Starting Frontend Server...
start "Frontend" cmd /c "cd frontend && npm run dev"

echo Waiting 6 seconds for servers to initialize...
timeout /t 6 /nobreak > NUL

echo Opening browser...
start http://localhost:5173

echo All processes started successfully!
