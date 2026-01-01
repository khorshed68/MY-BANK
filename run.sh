#!/bin/bash
# ============================================
# My Bank - Mac/Linux Run Script
# ============================================

echo "========================================"
echo "   MY BANK - Starting Application"
echo "========================================"
echo ""

# Set JavaFX path (Update this path if different)
if [ -z "$PATH_TO_FX" ]; then
    # Try common locations
    if [ -d "/usr/share/openjfx/lib" ]; then
        JAVAFX_PATH="/usr/share/openjfx/lib"
    elif [ -d "/usr/local/Cellar/openjfx" ]; then
        JAVAFX_PATH=$(find /usr/local/Cellar/openjfx -name "lib" | head -n 1)
    elif [ -d "$HOME/javafx-sdk-17/lib" ]; then
        JAVAFX_PATH="$HOME/javafx-sdk-17/lib"
    else
        echo "ERROR: JavaFX SDK not found!"
        echo "Please set PATH_TO_FX environment variable or install JavaFX"
        exit 1
    fi
else
    JAVAFX_PATH="$PATH_TO_FX"
fi

# Set SQLite JDBC path
SQLITE_JAR="lib/sqlite-jdbc-3.43.0.0.jar"

# Check if JavaFX exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "ERROR: JavaFX SDK not found at $JAVAFX_PATH"
    echo "Please install JavaFX SDK or set PATH_TO_FX environment variable"
    exit 1
fi

# Check if SQLite JDBC exists
if [ ! -f "$SQLITE_JAR" ]; then
    echo "ERROR: SQLite JDBC driver not found at $SQLITE_JAR"
    echo "Please download sqlite-jdbc jar and place it in lib folder"
    exit 1
fi

# Create output directory if it doesn't exist
mkdir -p out

# Create database directory if it doesn't exist
mkdir -p database

echo "Compiling Java files..."
echo ""

# Compile all Java files
javac --module-path "$JAVAFX_PATH" \
      --add-modules javafx.controls,javafx.fxml \
      -cp "$SQLITE_JAR" \
      -d out \
      src/main/java/com/mybank/*.java \
      src/main/java/com/mybank/controllers/*.java \
      src/main/java/com/mybank/models/*.java \
      src/main/java/com/mybank/database/*.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo "Compilation successful!"
echo ""
echo "Copying resources..."

# Copy resources to output directory
cp -r src/main/resources/* out/ 2>/dev/null || :

echo ""
echo "Starting My Bank application..."
echo ""

# Run the application
java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "out:$SQLITE_JAR" \
     com.mybank.Main

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Application failed to start!"
    exit 1
fi

echo ""
echo "Application closed."
