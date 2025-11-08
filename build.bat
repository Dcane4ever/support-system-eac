@echo off
title Building Customer Service AIMS
color 0B
cls

echo ================================================================
echo    BUILDING CUSTOMER SERVICE AIMS
echo ================================================================
echo.
echo This will create a runnable JAR file...
echo.

REM Clean and build
echo [1/2] Cleaning old builds...
call mvn clean

echo.
echo [2/2] Building JAR file (this may take a few minutes)...
call mvn package -DskipTests

echo.
echo ================================================================
if exist "target\customer-service-aims.jar" (
    echo    BUILD SUCCESSFUL!
    echo ================================================================
    echo.
    echo JAR file created at:
    echo    target\customer-service-aims.jar
    echo.
    echo To run the application:
    echo    1. Double-click "run.bat" file
    echo    2. Or use: java -jar target\customer-service-aims.jar
    echo.
    echo The application will start at:
    echo    http://localhost:8080
    echo.
) else (
    echo    BUILD FAILED!
    echo ================================================================
    echo.
    echo Please check the error messages above.
    echo.
)

pause
