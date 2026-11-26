@echo off
REM Builds the project and starts the server.
REM Double-click it, or run:  run.bat
cd /d "%~dp0"

call build.bat nopause
if errorlevel 1 (
    pause
    exit /b 1
)

REM the hardware script writes data\cars.csv here
if not exist data mkdir data

echo.
echo Starting the server on http://localhost:8080   Ctrl+C stops it
echo.
java -cp "out;lib/*" Server

echo.
echo The server stopped.
pause
