@echo off
title EduCore - Student Management System
cd /d "%~dp0"

echo =======================================================
echo   EduCore Student Management System
echo   Compiling and Launching Application...
echo =======================================================

if not exist "bin" mkdir bin
if not exist "data" mkdir data

REM Compile Java source files
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 21+ is in your PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo [OK] Compilation successful. Starting GUI application...
start javaw -cp bin com.sms.Main
exit /b 0
