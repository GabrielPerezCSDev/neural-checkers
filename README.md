# Neural Checkers: A Monte Carlo Tree Search Implementation

## Overview
Neural Checkers is a backend implementation of the classic board game Checkers featuring an AI opponent powered by Monte Carlo Tree Search (MCTS). This repository contains the server-side code, providing game logic and AI functionality. The frontend implementation is integrated into my portfolio website, where you can try the live demo.

## Live Demo
You can experience the full application at my portfolio website:
[Play Neural Checkers](https://gabrielperezcsdev.github.io/Portfolio/)

## Features
- Intelligent AI opponent using Monte Carlo Tree Search
- Multiple difficulty levels
- Complete game state management
- Legal move validation
- Game session handling
- RESTful API endpoints

## Technology Stack
### Backend (This Repository)
- Java
- HTTP Server with custom implementation
- Thread management for game sessions
- RESTful API endpoints
- Monte Carlo Tree Search implementation

### Frontend (Available in Portfolio)
- React + TypeScript
- Interactive board visualization
- Real-time game state updates
- Cyberpunk-themed UI

## Key Components
### AI Implementation
- Monte Carlo Tree Search algorithm
- Node expansion and backpropagation
- Dynamic difficulty adjustment
- Multi-threaded simulation

### Game Logic
- Complete checkers rule implementation
- Legal move validation
- King piece promotion
- Multiple jump handling
- Game state tracking

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven

## Installation and Setup

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Bash shell (for build script)

### Environment Configuration
Create a `.env` file in the root directory:
```env
PORT=9000
HOST=localhost  # Use 0.0.0.0 for network access
```

### Project Structure
```
neural-checkers/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── checkers/
│       └── Main.java
├── bin/
├── build.sh
└── .env
```

### Building and Running
1. Clone the repository
```bash
git clone https://github.com/yourusername/neural-checkers-backend.git
```

2. Make the build script executable
```bash
chmod +x build.sh
```

3. Run the build script
```bash
./build.sh
```

The build script will:
- Clean previous builds
- Create a new bin directory
- Compile all Java files
- Run the program automatically

### Build Script Details
```bash
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

# Find all Java files under src/main/java/com/checkers
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
```


## API Endpoints
- `PUT /` - Establish connection
- `PUT /start` - Start new game
- `POST /player-move` - Make a player move
- `PUT /make-ai-move` - Trigger AI move
- `PUT /legal-moves` - Get legal moves for selected piece
- `POST /get-board` - Get current board state
- `PUT /game-status` - Check game status
- `POST /reset` - Reset current game
- `POST /stop` - Stop current game

## Game Rules
- Red moves first
- Pieces move diagonally forward
- Kings can move forward and backward
- Jumps are mandatory
- Multiple jumps must be completed in a single turn
- Pieces are promoted to kings upon reaching the opposite end

## Architecture
The server implements:
- RESTful API endpoints for game actions
- Stateful game sessions
- Real-time move validation
- Thread management for concurrent games
- AI move calculation using MCTS
