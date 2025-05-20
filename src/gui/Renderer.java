package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import algorithm.AStar;
import algorithm.Dijkstra;
import algorithm.GBFS;
import algorithm.UCS;
import core.Board;
import core.GameState;
import core.Move;

public class Renderer {
    
    public interface StepChangeListener {
        void onStepChange(int stepIndex);
    }
    
    private List<StepChangeListener> stepChangeListeners = new ArrayList<>();
    
    public void addStepChangeListener(StepChangeListener listener) {
        stepChangeListeners.add(listener);
    }
    
    private BoardPane boardPane;
    private char[][] currentBoard;
    private List<MoveStep> solutionSteps;
    private int currentStepIndex = -1;
    private Timeline animation;
    
    private int totalMoves = 0;
    private int nodesVisited = 0;
    private long executionTime = 0;
    
    public Renderer(BoardPane boardPane) {
        this.boardPane = boardPane;
        this.solutionSteps = new ArrayList<>();
        setupAnimation();
    }
    
    private void setupAnimation() {
        animation = new Timeline(
            new KeyFrame(Duration.millis(1000), e -> showNextMove())
        );
        animation.setCycleCount(Timeline.INDEFINITE);
    }

    public void loadPuzzleFromFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String[] dimensions = reader.readLine().split(" ");
            if (dimensions.length < 2) {
                throw new IllegalArgumentException("Invalid dimensions format");
            }
            
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);
            
            System.out.println("Loading board with dimensions: " + rows + "x" + cols);
            
            // Skip baris kedua yang berisi jumlah piece
            String secondLine = reader.readLine();
            boolean isNumericLine = false;
            
            try {
                Integer.parseInt(secondLine.trim());
                isNumericLine = true;
            } catch (NumberFormatException e) {
                // Bukan angka, jadi ini adalah baris pertama board
                isNumericLine = false;
            }
            
            // Baca seluruh konfigurasi board
            List<String> boardLines = new ArrayList<>();
            if (!isNumericLine) {
                boardLines.add(secondLine);
            }
            
            for (int i = (isNumericLine ? 0 : 1); i < rows; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("File terlalu pendek, kurang baris");
                }
                boardLines.add(line);
            }
            
            // Cari posisi K dan column pertama dengan isi
            boolean exitExists = false;
            int exitRow = -1, exitCol = -1;
            int firstNonSpaceCol = Integer.MAX_VALUE;
            int lastNonSpaceCol = -1;
            
            // Cari first non-space column dan K position
            for (int i = 0; i < boardLines.size(); i++) {
                String line = boardLines.get(i);
                
                // Find first non-space position
                for (int j = 0; j < line.length(); j++) {
                    if (line.charAt(j) != ' ') {
                        firstNonSpaceCol = Math.min(firstNonSpaceCol, j);
                        lastNonSpaceCol = Math.max(lastNonSpaceCol, j);
                        
                        // Check if this is K
                        if (line.charAt(j) == 'K') {
                            exitExists = true;
                            exitRow = i;
                            exitCol = j;
                        }
                    }
                }
            }
            
            // Jika tidak ada konten selain spasi
            if (firstNonSpaceCol == Integer.MAX_VALUE) {
                firstNonSpaceCol = 0;
            }
            
            // Menghitung offset kiri dan kanan
            int leftOffset = 0;
            int rightOffset = 0;
            
            if (exitExists) {
                if (exitCol < firstNonSpaceCol) {
                    // K di sebelah kiri semua konten
                    leftOffset = 1;
                } else if (exitCol > lastNonSpaceCol) {
                    // K di sebelah kanan semua konten
                    rightOffset = 1;
                }
            }
            
            // Mencari primary piece dan orientasinya
            boolean foundPrimaryPiece = false;
            boolean isHorizontal = false;
            int pRow = -1, pCol = -1;
            
            for (int i = 0; i < boardLines.size(); i++) {
                String line = boardLines.get(i);
                int pIndex = line.indexOf('P');
                if (pIndex >= 0) {
                    foundPrimaryPiece = true;
                    pRow = i;
                    pCol = pIndex - firstNonSpaceCol + leftOffset;
                    
                    // Periksa orientasi
                    if (pIndex + 1 < line.length() && line.charAt(pIndex + 1) == 'P') {
                        isHorizontal = true;
                    } else if (pIndex > 0 && line.charAt(pIndex - 1) == 'P') {
                        isHorizontal = true;
                    } else {
                        isHorizontal = false;
                    }
                    break;
                }
            }
            
            // Jika tidak ada P atau K, cari orientasi dari baris lain
            if (foundPrimaryPiece && !isHorizontal) {
                for (int i = 0; i < boardLines.size(); i++) {
                    if (i == pRow) continue; // Skip baris primary piece
                    String line = boardLines.get(i);
                    if (line.length() > exitCol && line.charAt(exitCol) == 'P') {
                        // P ditemukan pada kolom yang sama di baris berbeda
                        isHorizontal = false;
                        break;
                    }
                }
            }
            
            System.out.println("Primary piece found at [" + pRow + "," + pCol + "], Orientation: " + 
                            (isHorizontal ? "Horizontal" : "Vertical"));
            System.out.println("First non-space column: " + firstNonSpaceCol + ", Last non-space column: " + lastNonSpaceCol);
            
            // Menentukan dimensi final board
            int effectiveContentWidth = lastNonSpaceCol - firstNonSpaceCol + 1;
            int effectiveContentHeight = boardLines.size();
            
            // Calculate final dimensions ensuring we have full cols with possible exit points
            int finalRows = Math.max(rows, effectiveContentHeight + (isHorizontal ? 0 : 1));
            int finalCols = Math.max(cols, effectiveContentWidth + leftOffset + rightOffset);
            
            // Jika belum ada exit, tambahkan satu kolom/baris untuk exit berdasarkan orientasi
            if (!exitExists && foundPrimaryPiece) {
                if (isHorizontal) {
                    finalCols++; // Tambah kolom di kanan untuk exit
                    rightOffset = 1;
                } else {
                    finalRows++; // Tambah baris di bawah untuk exit
                }
            }
            
            System.out.println("Calculated dimensions: " + finalRows + "x" + finalCols + 
                            " (leftOffset=" + leftOffset + ", rightOffset=" + rightOffset + ")");
            
            // Buat board dengan dimensi final
            currentBoard = new char[finalRows][finalCols];
            
            // Isi dengan '.'
            for (int i = 0; i < finalRows; i++) {
                for (int j = 0; j < finalCols; j++) {
                    currentBoard[i][j] = '.';
                }
            }
            
            // Salin konten dari input ke board dengan offset yang tepat
            for (int i = 0; i < boardLines.size() && i < rows; i++) {
                String line = boardLines.get(i);
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (c == ' ') continue; // Skip spasi
                    
                    // Hitung posisi di board baru dengan offset
                    int newCol = j - firstNonSpaceCol + leftOffset;
                    
                    if (newCol >= 0 && newCol < finalCols) {
                        currentBoard[i][newCol] = c;
                    }
                }
            }
            
            // Jika exit belum ada, tambahkan berdasarkan orientasi primary piece
            if (!exitExists && foundPrimaryPiece) {
                if (isHorizontal) {
                    currentBoard[pRow][finalCols - 1] = 'K';
                    exitRow = pRow;
                    exitCol = finalCols - 1;
                    System.out.println("Added horizontal exit at [" + exitRow + "," + exitCol + "]");
                } else {
                    currentBoard[finalRows - 1][pCol] = 'K';
                    exitRow = finalRows - 1;
                    exitCol = pCol;
                    System.out.println("Added vertical exit at [" + exitRow + "," + exitCol + "]");
                }
            }
            
            // Pastikan K diletakkan dengan benar jika sudah ada
            if (exitExists) {
                // Tentukan posisi K dalam board baru
                int newExitRow = exitRow;
                int newExitCol = exitCol - firstNonSpaceCol + leftOffset;
                
                // Pastikan posisi valid
                if (newExitRow >= 0 && newExitRow < finalRows && 
                    newExitCol >= 0 && newExitCol < finalCols) {
                    // Hapus K dari posisi lain (jika ada)
                    for (int i = 0; i < finalRows; i++) {
                        for (int j = 0; j < finalCols; j++) {
                            if (currentBoard[i][j] == 'K' && (i != newExitRow || j != newExitCol)) {
                                currentBoard[i][j] = '.';
                            }
                        }
                    }
                    
                    // Tempatkan K di posisi yang benar
                    currentBoard[newExitRow][newExitCol] = 'K';
                    
                    System.out.println("Placed exit at [" + newExitRow + "," + newExitCol + "]");
                }
            }
            
            // Bersihkan kolom/baris exit - tidak boleh ada piece lain di kolom/baris K kecuali primary piece
            for (int i = 0; i < finalRows; i++) {
                for (int j = 0; j < finalCols; j++) {
                    if (currentBoard[i][j] == 'K') {
                        // Jika horizontal, bersihkan kolom K
                        if (isHorizontal) {
                            for (int row = 0; row < finalRows; row++) {
                                if (row != i && currentBoard[row][j] != 'P') {
                                    currentBoard[row][j] = '.';
                                }
                            }
                        } 
                        // Jika vertical, bersihkan baris K
                        else {
                            for (int col = 0; col < finalCols; col++) {
                                if (col != j && currentBoard[i][col] != 'P') {
                                    currentBoard[i][col] = '.';
                                }
                            }
                        }
                    }
                }
            }
            
            // Debug: Tampilkan board final
            System.out.println("Final board contents:");
            for (int i = 0; i < currentBoard.length; i++) {
                System.out.println(new String(currentBoard[i]));
            }
            
            // Temukan posisi exit
            exitRow = -1;
            exitCol = -1;
            for (int i = 0; i < finalRows; i++) {
                for (int j = 0; j < finalCols; j++) {
                    if (currentBoard[i][j] == 'K') {
                        exitRow = i;
                        exitCol = j;
                        break;
                    }
                }
                if (exitRow != -1) break;
            }
            
            System.out.println("Final exit position: row=" + exitRow + ", col=" + exitCol);
            System.out.println("Grid dimensions: rows=" + finalRows + ", cols=" + finalCols);
            
            // Inisialisasi board pada GUI
            boardPane.initializeBoard(currentBoard);
            
            solutionSteps.clear();
            currentStepIndex = -1;
            
            solutionSteps.add(new MoveStep(copyBoard(currentBoard), null, null, 0));
            
            System.out.println("File berhasil dimuat. Board " + finalRows + "x" + finalCols);
            
        } catch (IOException e) {
            System.err.println("Error loading puzzle file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error loading puzzle file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
            }
        }
    }

    public boolean solvePuzzle(String algorithm, String heuristic) {
        System.out.println("Start solving puzzle with algorithm: " + algorithm + 
                        ", heuristic: " + (heuristic != null ? heuristic : "N/A"));
        try {
            // Debug board sebelum membuat file
            debugBoard(currentBoard);
            
            // Buat file temporary untuk konfigurasi board
            File tempFile = File.createTempFile("rushHourBoard", ".txt");
            tempFile.deleteOnExit();
            
            try (FileWriter writer = new FileWriter(tempFile)) {
                // Tulis dimensi board
                writer.write(currentBoard.length + " " + currentBoard[0].length + "\n");
                
                // Hitung jumlah kendaraan selain P
                int numNonPrimaryPieces = countNonPrimaryPieces();
                writer.write(numNonPrimaryPieces + "\n");
                
                // Tulis konfigurasi board DENGAN K yang sudah ditetapkan
                for (int i = 0; i < currentBoard.length; i++) {
                    writer.write(new String(currentBoard[i]) + "\n");
                }
                
                // Tambahkan algoritma dan heuristik ke file
                writer.write(algorithm + "\n");
                if (!"dijkstra".equals(algorithm.toLowerCase()) && 
                    !"ucs".equals(algorithm.toLowerCase()) && 
                    heuristic != null) {
                    writer.write(heuristic + "\n");
                }
            }
            
            System.out.println("Temporary file created: " + tempFile.getAbsolutePath());
            
            // Debug file yang dibuat
            debugReadFile(tempFile);
            
            // Load board untuk algoritma
            Board coreBoard = new Board(tempFile.getAbsolutePath());
            System.out.println("Core board loaded");
            
            // Debug: cetak board setelah dimuat
            coreBoard.printBoard(null);
            
            // Jalankan algoritma sesuai pilihan
            GameState solution = null;
            String algorithmLower = algorithm.toLowerCase().trim();
            
            System.out.println("Running algorithm: " + algorithmLower);
            
            if ("astar".equals(algorithmLower)) {
                System.out.println("Using A* algorithm with " + 
                                (heuristic != null ? heuristic : "manhattan") + " heuristic");
                AStar astar = new AStar(heuristic != null ? heuristic : "manhattan");
                solution = astar.solve(coreBoard);
                nodesVisited = astar.getNodesVisited();
                executionTime = (long) astar.getExecutionTime();

            } else if ("dijkstra".equals(algorithmLower)) {
                System.out.println("Using Dijkstra algorithm");
                Dijkstra dijkstra = new Dijkstra();
                solution = dijkstra.solve(coreBoard);
                nodesVisited = dijkstra.getNodesVisited();
                executionTime = (long) dijkstra.getExecutionTime();

            } else if ("ucs".equals(algorithmLower)) {
                System.out.println("Using UCS algorithm");
                UCS ucs = new UCS(); // FIX: gunakan UCS, bukan Dijkstra
                solution = ucs.solve(coreBoard);
                nodesVisited = ucs.getNodesVisited();
                executionTime = (long) ucs.getExecutionTime();

            } else if ("gbfs".equals(algorithmLower) || "greedy".equals(algorithmLower)) {
                System.out.println("Using GBFS algorithm with " + 
                                (heuristic != null ? heuristic : "manhattan") + " heuristic");
                GBFS gbfs = new GBFS(heuristic != null ? heuristic : "manhattan");
                solution = gbfs.solve(coreBoard);
                nodesVisited = gbfs.getNodesVisited();
                executionTime = (long) gbfs.getExecutionTime();

            } else {
                // Fallback
                System.out.println("Unknown algorithm: " + algorithmLower + ". Using A* as fallback");
                AStar astarFallback = new AStar("manhattan");
                solution = astarFallback.solve(coreBoard);
                nodesVisited = astarFallback.getNodesVisited();
                executionTime = (long) astarFallback.getExecutionTime();
            }
            
            if (solution != null) {
                System.out.println("Solution found with " + solution.getMoves().size() + " moves!");
                // Konversi solusi ke format GUI
                processAlgorithmSolution(solution);
                return true;
            } else {
                System.out.println("No solution found!");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error solving puzzle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void debugBoard(char[][] board) {
        System.out.println("DEBUG: Board state (" + board.length + "x" + board[0].length + "):");
        for (int i = 0; i < board.length; i++) {
            System.out.println(new String(board[i]));
        }
        
        // Cek primary piece dan exit
        boolean foundP = false;
        boolean foundK = false;
        int pRow = -1, pCol = -1;
        int kRow = -1, kCol = -1;
        boolean isHorizontal = false;
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 'P') {
                    foundP = true;
                    if (pRow == -1) {
                        pRow = i;
                        pCol = j;
                    }
                    // Cek orientasi
                    if (j > 0 && j < board[i].length && board[i][j-1] == 'P') isHorizontal = true;
                    if (j < board[i].length-1 && board[i][j+1] == 'P') isHorizontal = true;
                }
                if (board[i][j] == 'K') {
                    foundK = true;
                    kRow = i;
                    kCol = j;
                }
            }
        }
        
        System.out.println("Primary piece (P): " + (foundP ? "Found at [" + pRow + "," + pCol + "]" : "NOT FOUND!"));
        System.out.println("Primary piece orientation: " + (isHorizontal ? "Horizontal" : "Vertical"));
        System.out.println("Exit (K): " + (foundK ? "Found at [" + kRow + "," + kCol + "]" : "NOT FOUND!"));
        
        // Verifikasi kesesuaian orientasi dan posisi exit
        if (foundP && foundK) {
            boolean validExit = false;
            if (isHorizontal && kRow == pRow) validExit = true;
            if (!isHorizontal && kCol == pCol) validExit = true;
            
            System.out.println("Exit alignment with primary piece: " + (validExit ? "Valid" : "INVALID!"));
        }
    }

    private void debugReadFile(File file) {
        System.out.println("DEBUG: Reading file " + file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println("Line " + (++lineNum) + ": " + line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
        
    private int countNonPrimaryPieces() {
        Set<Character> uniquePieces = new HashSet<>();
        for (int i = 0; i < currentBoard.length; i++) {
            for (int j = 0; j < currentBoard[0].length; j++) {
                char piece = currentBoard[i][j];
                if (piece != '.' && piece != 'K' && piece != 'P') {
                    uniquePieces.add(piece);
                }
            }
        }
        return uniquePieces.size();
    }
    
    private void processAlgorithmSolution(GameState solution) {
        System.out.println("Processing solution with " + solution.getMoves().size() + " moves");
        
        // Reset solution steps
        solutionSteps.clear();
        currentStepIndex = -1;
        
        // Add initial state
        solutionSteps.add(new MoveStep(copyBoard(currentBoard), null, null, 0));
        
        // Current board state that will be updated with each move
        char[][] boardState = copyBoard(currentBoard);
        List<Move> moves = solution.getMoves();
        
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            char piece = move.getPieceId();
            String direction = convertDirectionToGui(move.getDirection());
            int steps = 1; // Default to 1 step
            
            // Try to get steps if available
            try {
                java.lang.reflect.Method getStepsMethod = move.getClass().getMethod("getSteps");
                steps = (int) getStepsMethod.invoke(move);
            } catch (Exception e) {
                // Ignore if getSteps method is not available
                steps = 1;
            }
            
            System.out.println("Move " + (i+1) + ": " + piece + " " + direction + " " + steps);
            
            // Apply move to current board state
            char[][] newBoardState = copyBoard(boardState);
            applyMoveToBoard(newBoardState, piece, direction, steps);
            
            // Add to solution steps with current piece and move info
            solutionSteps.add(new MoveStep(newBoardState, piece, direction, steps, i + 1));
            
            // Update board state for next move
            boardState = newBoardState;
        }
        
        totalMoves = moves.size();
    }
    
    private void applyMoveToBoard(char[][] board, char piece, String direction, int steps) {
        // Identify all cells containing the piece
        List<int[]> pieceCells = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col] == piece) {
                    pieceCells.add(new int[]{row, col});
                }
            }
        }
        
        if (pieceCells.isEmpty()) return;
        
        // Determine if piece is horizontal (all cells have same row) or vertical
        boolean isHorizontal = true;
        int firstRow = pieceCells.get(0)[0];
        for (int[] cell : pieceCells) {
            if (cell[0] != firstRow) {
                isHorizontal = false;
                break;
            }
        }
        
        // Find min and max coordinates (top-left and bottom-right)
        int minRow = Integer.MAX_VALUE, minCol = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE, maxCol = Integer.MIN_VALUE;
        
        for (int[] cell : pieceCells) {
            minRow = Math.min(minRow, cell[0]);
            minCol = Math.min(minCol, cell[1]);
            maxRow = Math.max(maxRow, cell[0]);
            maxCol = Math.max(maxCol, cell[1]);
        }
        
        // Clear current piece positions
        for (int[] cell : pieceCells) {
            board[cell[0]][cell[1]] = '.';
        }
        
        // Calculate new position based on direction and steps
        int newMinRow = minRow;
        int newMinCol = minCol;
        
        if (isHorizontal) {
            if (direction.equals("left")) {
                newMinCol = minCol - steps; // Move left by steps
            } else if (direction.equals("right")) {
                newMinCol = minCol + steps; // Move right by steps
            }
        } else { // vertical
            if (direction.equals("up")) {
                newMinRow = minRow - steps; // Move up by steps
            } else if (direction.equals("down")) {
                newMinRow = minRow + steps; // Move down by steps
            }
        }
        
        // Place piece at new position
        int pieceHeight = maxRow - minRow + 1;
        int pieceWidth = maxCol - minCol + 1;
        
        for (int i = 0; i < pieceHeight; i++) {
            for (int j = 0; j < pieceWidth; j++) {
                if (newMinRow + i >= 0 && newMinRow + i < board.length && 
                    newMinCol + j >= 0 && newMinCol + j < board[0].length) {
                    board[newMinRow + i][newMinCol + j] = piece;
                }
            }
        }
    }

    private void displayMove(MoveStep step) {
        // Hanya update board dengan board state yang baru
        boardPane.updateBoard(step.board);
        
    }
    
    private String convertDirectionToGui(String algorithmDirection) {
        switch (algorithmDirection.toLowerCase()) {
            case "atas": return "up";
            case "bawah": return "down";
            case "kiri": return "left";
            case "kanan": return "right";
            default: return algorithmDirection;
        }
    }
    
    public void showPreviousMove() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            displayMove(solutionSteps.get(currentStepIndex));
            
        }
    }
    
    public void showNextMove() {
        if (currentStepIndex < solutionSteps.size() - 1) {
            currentStepIndex++;
            displayMove(solutionSteps.get(currentStepIndex));
            
        } else {
            animation.stop();
        }
    }
    
    public void playAnimation() {
        currentStepIndex = 0;
        displayMove(solutionSteps.get(0));
        
        animation.play();
    }
    
    public void stopAnimation() {
        animation.stop();
    }
    
    public void jumpToStep(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < solutionSteps.size()) {
            currentStepIndex = stepIndex;
            displayMove(solutionSteps.get(currentStepIndex));
            
        }
    }
    
    // Updating MoveStep class to include steps
    public class MoveStep {
        char[][] board;
        Character piece;
        String direction;
        int steps = 1;
        int moveNumber;
        
        MoveStep(char[][] board, Character piece, String direction, int moveNumber) {
            this(board, piece, direction, 1, moveNumber);
        }
        
        MoveStep(char[][] board, Character piece, String direction, int steps, int moveNumber) {
            this.board = board;
            this.piece = piece;
            this.direction = direction;
            this.steps = steps;
            this.moveNumber = moveNumber;
        }
    }
    
    private char[][] copyBoard(char[][] original) {
        if (original == null) return null;
        char[][] copy = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
    
    public int getTotalMoves() {
        return totalMoves;
    }
    
    public int getNodesVisited() {
        return nodesVisited;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void loadPuzzleFromFile(String filePath) {
        loadPuzzleFromFile(new File(filePath));
    }
    
    public int getCurrentStepIndex() {
        return currentStepIndex;
    }
    
    public int getTotalSteps() {
        return solutionSteps.size();
    }
}