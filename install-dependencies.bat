@echo off
REM ========================================
REM Maven Build Script for My Bank
REM ========================================

echo.
echo ========================================
echo   Installing My Bank Dependencies
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo.
    echo Please choose one of these options:
    echo.
    echo OPTION 1: Install Maven
    echo   1. Download from: https://maven.apache.org/download.cgi
    echo   2. Extract to C:\Program Files\Maven
    echo   3. Add to PATH: C:\Program Files\Maven\bin
    echo   4. Restart terminal and run this script again
    echo.
    echo OPTION 2: Use IntelliJ IDEA
    echo   1. Open the project in IntelliJ IDEA
    echo   2. Right-click on pom.xml
    echo   3. Select "Maven" ^> "Reload Project"
    echo   4. IntelliJ will automatically download dependencies
    echo.
    echo OPTION 3: Use Eclipse
    echo   1. Open the project in Eclipse
    echo   2. Right-click on project
    echo   3. Select "Maven" ^> "Update Project"
    echo   4. Eclipse will automatically download dependencies
    echo.
    pause
    exit /b 1
)

echo [INFO] Maven found! Starting build...
echo.

REM Navigate to project directory
cd /d "%~dp0"

REM Run Maven clean install
echo [INFO] Running: mvn clean install -DskipTests
echo.
call mvn clean install -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo Dependencies have been downloaded:
    echo   - JavaMail API (for email sending^)
    echo   - Twilio SDK (for SMS sending^)
    echo.
    echo Next steps:
    echo   1. Configure notification.properties
    echo   2. Run the application
    echo   3. Test notifications
    echo.
    echo See NOTIFICATION_FIX_README.md for details
    echo ========================================
    echo.
) else (
    echo.
    echo ========================================
    echo   BUILD FAILED!
    echo ========================================
    echo.
    echo Please check the error messages above
    echo.
)

pause
