@echo off
REM Builds the whole project. Compiled files go to the "out" folder.
REM Double-click it, or run:  build.bat
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] javac not found. Install a JDK, then open a new terminal.
    goto fail
)

if not exist out mkdir out

echo Compiling...
REM Server.java is the start of the program, javac finds all the other files from it
javac -encoding UTF-8 -cp "lib/*" -sourcepath . -d out Server.java
if errorlevel 1 (
    echo.
    echo [ERROR] Build failed, read the errors above.
    goto fail
)

echo.
echo Build OK, the classes are in the out folder.
if /i not "%~1"=="nopause" pause
exit /b 0

:fail
if /i not "%~1"=="nopause" pause
exit /b 1
