#!/bin/bash

# Paths
SRC="src"
BIN="bin"

# Clean previous build
if [ -d "$BIN" ]; then
    echo "Cleaning previous build..."
    rm -rf "$BIN"
fi
mkdir "$BIN"

# Find all Java files under src/main/java/com/checkers and save them to a variable
CHECKERS_FILES=$(find "$SRC/main/java/com/checkers" -name "*.java")

# Compile Main.java and all Java files under src/main/java/com/checkers
echo "Compiling Java files..."
javac -d "$BIN" -sourcepath "$SRC" "$SRC/main/Main.java" $CHECKERS_FILES

# Check for compilation errors
if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

# Run the program
echo "Running the program..."
java -cp "$BIN" main.Main

