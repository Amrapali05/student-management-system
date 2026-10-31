@echo off
title Test EduCore - Student Management System
cd /d "%~dp0"

echo =======================================================
echo   Running Automated Test Suite
echo =======================================================

dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

java -cp bin com.sms.test.StudentSystemTest
pause
