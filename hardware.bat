@echo off
REM Reads the Arduino and writes data\cars.csv for the server.
REM Run it in its OWN window, next to the server window.
cd /d "%~dp0"

REM Change this to the lot name in your database
set "LOT=Lot A"

where python >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Python not found. Install Python from python.org and tick Add to PATH.
    pause
    exit /b 1
)

python -c "import serial" >nul 2>nul
if errorlevel 1 (
    echo Installing pyserial...
    python -m pip install pyserial
)

echo.
echo Close the Arduino Serial Monitor first, only one program can use the port.
set /p "PORT=Arduino COM port, for example COM3 [Enter = auto detect]: "

if "%PORT%"=="" (
    python hardware\serial_to_csv.py --lot "%LOT%"
) else (
    python hardware\serial_to_csv.py --port %PORT% --lot "%LOT%"
)

echo.
pause
