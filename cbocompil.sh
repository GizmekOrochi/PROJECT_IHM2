#!/bin/bash

# Set the directories
SRC_DIR="src"
BUILD_DIR="build"

# Path to the JavaFX SDK lib and bin directories (adjust if necessary)
JAVAFX_SDK="/home/lancelot/Desktop/zulu21.38.21"
JAVAFX_BIN="/home/lancelot/Desktop/zulu21.38.21/bin"

# Create the build directory if it doesn't exist
mkdir -p "$BUILD_DIR"

echo "Compiling source files..."
"$JAVAFX_BIN/javac" \
  --module-path "$JAVAFX_SDK" \
  --add-modules javafx.base,javafx.controls,javafx.fxml \
  -d "$BUILD_DIR" $(find "$SRC_DIR" -name "*.java")

if [ $? -ne 0 ]; then
    echo "Compilation failed. Check the errors above."
    exit 1
fi

echo "Compilation successful. Compiled files are in the '$BUILD_DIR' directory."

