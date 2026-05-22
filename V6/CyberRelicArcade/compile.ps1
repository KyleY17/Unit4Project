# Compile script for CyberRelicArcade
# This script compiles all Java source files to the bin directory

Set-Location $PSScriptRoot

# Check if javac is in PATH
try {
    $javacPath = (Get-Command javac -ErrorAction Stop).Source
    Write-Host "Found javac at: $javacPath" -ForegroundColor Green
} catch {
    Write-Host "Error: Java Development Kit (JDK) not found in PATH" -ForegroundColor Red
    Write-Host "Please install JDK 8 or higher and add it to your system PATH"
    Write-Host "Download from: https://www.oracle.com/java/technologies/downloads/" -ForegroundColor Cyan
    Read-Host "Press Enter to exit"
    exit 1
}

# Compile all Java files
Write-Host "Compiling Java source files..." -ForegroundColor Yellow
javac -source 11 -target 11 -d bin src\*.java

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Compilation successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "To run the game, use: java -cp bin CyberRelicArcade" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "Compilation failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Read-Host "Press Enter to exit"
