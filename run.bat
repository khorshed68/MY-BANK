@echo off
REM ============================================
REM My Bank - Windows Run Script
REM ============================================

echo ========================================
echo    MY BANK - Starting Application
echo ========================================
echo.

REM Set JavaFX path (Update this path if different)
set JAVAFX_PATH=C:\Program Files\Java\javafx-sdk-17\lib

REM Set SQLite JDBC path
set SQLITE_JAR=lib\sqlite-jdbc-3.43.0.0.jar

REM Check if JavaFX exists
if not exist "%JAVAFX_PATH%" (
    echo ERROR: JavaFX SDK not found at %JAVAFX_PATH%
    echo Please install JavaFX SDK or update JAVAFX_PATH in this script
    pause
    exit /b 1
)

REM Check if SQLite JDBC exists
if not exist "%SQLITE_JAR%" (
    echo ERROR: SQLite JDBC driver not found at %SQLITE_JAR%
    echo Please download sqlite-jdbc jar and place it in lib folder
    pause
    exit /b 1
)

REM Create output directory if it doesn't exist
if not exist "out" mkdir out

REM Create database directory if it doesn't exist
if not exist "database" mkdir database

echo Compiling Java files...
echo.

REM Compile all Java files
javac --module-path "%JAVAFX_PATH%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -cp "%SQLITE_JAR%" ^
      -d out ^
      src\main\java\com\mybank\*.java ^
      src\main\java\com\mybank\controllers\*.java ^
      src\main\java\com\mybank\models\*.java ^
      src\main\java\com\mybank\database\*.java ^
      src\main\java\com\mybank\services\*.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Copying resources...

REM Copy resources to output directory
xcopy /E /I /Y src\main\resources out\resources >nul 2>&1

echo.
echo Starting My Bank application...
echo.

REM Run the application
java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     --add-opens java.base/java.lang=ALL-UNNAMED ^
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED ^
     -XX:+IgnoreUnrecognizedVMOptions ^
     -Djava.util.logging.config.file=logging.properties ^
     -cp "out;%SQLITE_JAR%" ^
     com.mybank.Main

if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start!
    pause
    exit /b 1
)

echo.
echo Application closed.
pause
