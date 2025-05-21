# Rush Hour Puzzle Solver

![RushHour](resources/rushhour.gif)

## Program Description
This program is an implementation of a solver for the Rush Hour puzzle game using various pathfinding algorithms. Rush Hour is a puzzle game where players must move the red car (primary piece) to the exit through traffic congestion by sliding other vehicles that block the way. The program provides implementations of A*, Greedy Best-First Search (GBFS), Uniform Cost Search (UCS), and Dijkstra algorithms to solve the puzzle with a minimal number of moves.

## Requirements
- Java JDK 11 or newer
- JavaFX SDK 17 or newer
- Operating systems: Windows, macOS, or Linux

# Rush Hour Puzzle Solver - Quick Start Guide

This guide provides step-by-step instructions to get the Rush Hour Puzzle Solver up and running on your system.

## Windows Setup

### Step 1: Install Java
1. Download and install Java Development Kit (JDK) 11 or higher from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)
2. Verify installation by opening Command Prompt and typing:
   ```
   java -version
   javac -version
   ```

### Step 2: Download JavaFX
1. Download JavaFX SDK from [Gluon's website](https://gluonhq.com/products/javafx/)
2. Extract the ZIP file to a location of your choice (e.g., `C:\Program Files\JavaFX\javafx-sdk-21.0.7`)

### Step 3: Set Up Project
1. Clone or extract the project to your computer
2. Navigate to the project directory using Command Prompt

### Step 4: Configure JavaFX Path
You have two options:

**Option A: Copy JavaFX libraries to project**
1. Create a `lib` folder in the project root directory:
   ```
   mkdir lib
   ```
2. Copy all JAR files from the JavaFX SDK's `lib` folder to your project's `lib` folder
3. Make sure the Makefile has `JAVAFX_LIB = lib`

**Option B: Use JavaFX from installed location**
1. Edit the Makefile:
   - Find the line `JAVAFX_LIB = lib`
   - Change it to your JavaFX installation path, for example:
     ```
     JAVAFX_LIB = C:/Program Files/JavaFX/javafx-sdk-21.0.7/lib
     ```
   - Make sure to use forward slashes (/) in the path

### Step 5: Build and Run
1. Check if JavaFX is correctly configured:
   ```
   make check-javafx
   ```
2. Compile the program:
   ```
   make compile
   ```
3. Run the program:
   ```
   make run
   ```

## macOS Setup

### Step 1: Install Java
1. Install using Homebrew:
   ```
   brew install openjdk@17
   ```
   Or download from [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. Verify installation:
   ```
   java -version
   javac -version
   ```

### Step 2: Download JavaFX
1. Install using Homebrew:
   ```
   brew install openjfx
   ```
   Or download from [Gluon's website](https://gluonhq.com/products/javafx/) and extract to a location of your choice.

### Step 3: Set Up Project
1. Clone or extract the project to your computer
2. Open Terminal and navigate to the project directory

### Step 4: Configure JavaFX Path
You have two options:

**Option A: Copy JavaFX libraries to project**
1. Create a `lib` folder in the project root directory:
   ```
   mkdir lib
   ```
2. Copy all JAR files from the JavaFX SDK's `lib` folder to your project's `lib` folder:
   ```
   cp /path/to/javafx-sdk-21.0.7/lib/*.jar lib/
   ```
3. Make sure the Makefile has `JAVAFX_LIB = lib`

**Option B: Use JavaFX from installed location**
1. If you installed using Homebrew, it's typically at:
   ```
   JAVAFX_LIB = /usr/local/opt/openjfx/libexec/lib
   ```
   Or if you downloaded manually:
   ```
   JAVAFX_LIB = /Users/yourusername/javafx-sdk-21.0.7/lib
   ```

### Step 5: Build and Run
1. Check if JavaFX is correctly configured:
   ```
   make check-javafx
   ```
2. Compile the program:
   ```
   make compile
   ```
3. Run the program:
   ```
   make run
   ```

## How to Use
1. Once the program is running, the GUI will appear.
2. Click "Load Puzzle File" to load a puzzle file with the appropriate format (.txt)
3. Select an algorithm (A*, GBFS, UCS, or Dijkstra) from the dropdown menu
4. Select a heuristic if using A* or GBFS (Manhattan Distance, Blocking Heuristic, or Combined Heuristic)
5. Click "Solve Puzzle" to solve the puzzle
6. The solution animation will run automatically
7. You can use the "Previous" and "Next" buttons for step-by-step navigation
8. Solution statistics (number of moves, nodes visited, execution time) will be displayed

## Input File Format
The input file must be a text file (.txt) with the following format:
```
A B
N
board_configuration
```
Where:
- A B: board dimensions (A x B)
- N: number of vehicles besides the primary piece
- board_configuration: representation of the board where:
  - 'P': primary piece (the main car that needs to exit)
  - 'K': exit door
  - Other letters: other vehicles
  - '.': empty cell

Example input file:
```
6 6
12
AAB..F
..BCDF
GPPCDFK
GH.III
GHJ...
LLJMM.
```

## Authors

| Name | Student ID |
|------|------------|
| Adinda Putri | 13523071 |
| Shanice Feodora T. | 13523097 |

Class: 02 
Institution: Institut Teknologi Bandung

## Acknowledgements
This program was created to fulfill the requirements of Tugas Kecil 3 for the IF2211 Algorithm Strategies course, Semester II 2024/2025, Informatics Engineering Program, School of Electrical Engineering and Informatics, Institut Teknologi Bandung.
