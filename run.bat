@echo off
title Customer Service AIMS - HTTPS Voice Call Support
color 0A
cls

echo ================================================================
echo    CUSTOMER SERVICE AIMS - HTTPS Voice Call Support
echo ================================================================
echo.
echo Detecting network configuration...
echo.

REM Check if JAR file exists
if not exist "target\customer-service-aims.jar" (
    echo ERROR: JAR file not found!
    echo Please build the project first using: mvnw clean package
    echo.
    pause
    exit /b 1
)

REM Get Ethernet adapter IPv4 address (excluding Radmin VPN)
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /C:"Ethernet adapter Ethernet" /A:2 ^| findstr /C:"IPv4 Address"') do (
    set IP_ADDRESS=%%a
)

REM Clean up the IP address (remove spaces)
set IP_ADDRESS=%IP_ADDRESS: =%

if "%IP_ADDRESS%"=="" (
    echo WARNING: Could not detect Ethernet IP address!
    echo Using default binding (0.0.0.0)...
    set IP_ADDRESS=0.0.0.0
) else (
    echo Detected Ethernet IP: %IP_ADDRESS%
)

echo.
echo ================================================================
echo Starting HTTPS server...
echo ================================================================
echo.
echo Access the application at:
echo    - Local:   https://localhost:8443
echo    - Network: https://%IP_ADDRESS%:8443
echo.
echo NOTE: Your browser will show a security warning (self-signed certificate)
echo       Click "Advanced" and "Proceed" to continue
echo.
echo Press Ctrl+C to stop the application
echo ================================================================
echo.

REM Run the JAR file with the detected IP address
java -jar -Dserver.address=%IP_ADDRESS% target\customer-service-aims.jar

pause
