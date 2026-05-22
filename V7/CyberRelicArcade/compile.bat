@echo off
REM Compile script for CyberRelicArcade
REM This script compiles all Java source files to the bin directory

cd /d "%~dp0"

REM Check if javac is in PATH
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java Development Kit (JDK) not found in PATH
    echo Please install JDK 8 or higher and add it to your system PATH
    echo Download from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Compile all Java files
echo Compiling Java source files...
javac -source 11 -target 11 -d bin src\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    echo To run the game, use: java -cp bin CyberRelicArcade
) else (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

pause
