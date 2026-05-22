@echo off
REM Run script for CyberRelicArcade

cd /d "%~dp0"

REM Check if bin directory exists and has compiled classes
if not exist "bin\CyberRelicArcade.class" (
    echo Error: Compiled class files not found!
    echo Please run compile.bat first to compile the project.
    pause
    exit /b 1
)

REM Check if Java is available
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java Runtime not found in PATH
    echo Please install Java and add it to your system PATH
    pause
    exit /b 1
)

REM Run the game
echo Starting CYBER RELIC ARCADE...
java -cp bin CyberRelicArcade

