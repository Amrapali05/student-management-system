@echo off
title Build EduCore - Enterprise Student Management System
cd /d "%~dp0"

echo =======================================================
echo   EduCore Student Management System - Build System
echo   Maven and Standalone Java 21 Build Automation
echo =======================================================

if not exist "bin" mkdir bin
if not exist "data" mkdir data

REM Check if Maven is available in PATH
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Apache Maven detected. Building with Maven...
    call mvn clean package
    if %ERRORLEVEL% EQU 0 (
        echo [SUCCESS] Maven build and packaging complete!
        goto finish
    ) else (
        echo [WARNING] Maven build encountered an error. Falling back to direct javac...
    )
)

echo [1/3] Gathering Java source files from src/main/java...
dir /s /b src\main\java\*.java > sources.txt

echo [2/3] Compiling enterprise source code...
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 21+ is configured.
    pause
    exit /b %ERRORLEVEL%
)

REM Copy resources into bin directory
if exist "src\main\resources" (
    xcopy /s /y "src\main\resources\*" "bin\" >nul
)

echo [3/3] Packaging standalone executable JAR...
jar cfe student-management-system.jar com.sms.Main -C bin .

if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Jar packaging failed, but compiled classes are available in bin/
) else (
    echo [SUCCESS] Created student-management-system.jar
    echo You can double-click student-management-system.jar or run:
    echo   java -jar student-management-system.jar
)

:finish
echo.
echo Build finished successfully!
exit /b 0
