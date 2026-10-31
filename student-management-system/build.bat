@echo off
title Build EduCore - Student Management System
cd /d "%~dp0"

echo =======================================================
echo   EduCore Student Management System - Build System
echo =======================================================

if not exist "bin" mkdir bin
if not exist "data" mkdir data

echo [1/3] Gathering Java source files...
dir /s /b src\*.java > sources.txt

echo [2/3] Compiling source code...
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [3/3] Packaging standalone executable JAR...
jar cfe student-management-system.jar com.sms.Main -C bin com

if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Jar packaging failed, but compiled classes are available in bin/
) else (
    echo [SUCCESS] Created student-management-system.jar
    echo You can double-click student-management-system.jar or run:
    echo   java -jar student-management-system.jar
)

echo.
echo Build finished successfully!
pause
exit /b 0
