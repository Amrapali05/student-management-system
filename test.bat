@echo off
title Test EduCore - Enterprise Verification Test Suite
cd /d "%~dp0"

echo =======================================================
echo   Running Automated Enterprise Test Suite
echo =======================================================

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Running tests with Maven...
    call mvn test
    exit /b %ERRORLEVEL%
)

REM Direct standalone compilation & test execution
dir /s /b src\main\java\*.java src\test\java\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Test compilation failed.
    exit /b %ERRORLEVEL%
)

if exist "src\main\resources" (
    xcopy /s /y "src\main\resources\*" "bin\" >nul
)

java -cp bin com.sms.test.StudentSystemTest
exit /b %ERRORLEVEL%
