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
            
            // Parse dimensions
            String dimensionsLine = reader.readLine();
            if (dimensionsLine == null) {
                throw new IllegalArgumentException("File is empty or cannot be read.");
            }
            
            String[] dimensions = dimensionsLine.split("\\s+");
            if (dimensions.length < 2) {
                throw new IllegalArgumentException("Invalid dimensions format");
            }
            
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);
            
            System.out.println("Loading board with dimensions: " + rows + "x" + cols);
            
            // Skip baris kedua yang berisi jumlah piece
            String secondLine = reader.readLine();
            boolean isNumericLine = true;
            
            try {
                Integer.parseInt(secondLine.trim());
            } catch (NumberFormatException e) {
                isNumericLine = false;
            }
            
            // --- Ambil baris board, deteksi jika ada baris K di atas grid utama ---
            List<String> boardLines = new ArrayList<>();
            String line;
            int leftPadding = 0;
            boolean hasLeftK = false;
            boolean hasTopK = false; // Flag untuk K di baris pertama
            boolean exitExists = false;
            int exitRow = -1, exitCol = -1;

            // baris kedua sudah dicek, langsung lanjut baca
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                // Deteksi baris K di atas grid: hanya K atau hanya spasi+K
                if (boardLines.isEmpty() && (trimmed.equals("K") || (trimmed.length() > 1 && trimmed.replace("K", "").trim().isEmpty()))) {
                    hasTopK = true;
                    exitCol = line.indexOf('K');
                    System.out.println("K found in top row at col: " + exitCol);
                    continue; // SKIP baris ini, jangan masukin ke boardLines
                }
                boardLines.add(line);
                // Stop jika sudah cukup baris grid
                if (boardLines.size() >= rows) break;
            }
            
            // Cari minimum padding kiri di semua baris
            int minLeadingSpace = Integer.MAX_VALUE;
            for (String boardLine : boardLines) {
                if (boardLine.trim().isEmpty()) continue;
                int leadingSpaces = boardLine.indexOf(boardLine.trim().charAt(0));
                minLeadingSpace = Math.min(minLeadingSpace, leadingSpaces);
            }
            if (minLeadingSpace == Integer.MAX_VALUE) minLeadingSpace = 0;
            leftPadding = minLeadingSpace;
            
            System.out.println("Minimum leading spaces: " + leftPadding);
            
            // Mencari primary piece dan orientasinya
            boolean foundPrimaryPiece = false;
            boolean isHorizontal = false;
            int pRow = -1, pCol = -1;
            
            // TAHAP 0: Periksa K di baris pertama (SEBELUM grid normal) - baru
            if (boardLines.size() > 0) {
                String firstLine = boardLines.get(0);
                if (firstLine.contains("K") && firstLine.indexOf('K') <= firstLine.length() - 1) {
                    // K di baris pertama
                    exitExists = true;
                    exitRow = 0;
                    exitCol = firstLine.indexOf('K') - leftPadding;
                    hasTopK = true;
                    System.out.println("K found in first row at col " + exitCol);
                }
            }
            
            // TAHAP 1: Periksa K di awal baris (dengan spasi awal)
            if (!exitExists) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.trim().startsWith("K")) {
                        exitExists = true;
                        exitRow = i;
                        
                        // Hitung posisi K relatif terhadap leftPadding
                        int kPos = currentLine.indexOf('K');
                        if (kPos - leftPadding <= 0) {
                            // K berada di awal setelah padding
                            exitCol = 0;
                            hasLeftK = true;
                            System.out.println("K found at start of row " + i + " with left padding " + leftPadding);
                        } else {
                            // K berada di posisi lain
                            exitCol = kPos - leftPadding;
                            System.out.println("K found in row " + i + " at col " + exitCol);
                        }
                        break;
                    }
                }
            }
            
            // TAHAP 2: Periksa K di dalam board normal
            if (!exitExists) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    int kIndex = currentLine.indexOf('K');
                    if (kIndex != -1) {
                        exitExists = true;
                        exitRow = i;
                        exitCol = kIndex - leftPadding;
                        
                        // Jika K berada di posisi awal setelah padding
                        if (kIndex - leftPadding <= 0) {
                            hasLeftK = true;
                            exitCol = 0;
                        }
                        
                        System.out.println("K found in board at row " + i + ", col " + exitCol);
                        break;
                    }
                }
            }
            
            // TAHAP 3: Periksa K di baris tambahan di bawah
            if (!exitExists && boardLines.size() > rows) {
                for (int i = rows; i < boardLines.size(); i++) {
                    String currentLine = boardLines.get(i);
                    int kIndex = currentLine.indexOf('K');
                    if (kIndex != -1) {
                        exitExists = true;
                        exitRow = i;
                        exitCol = kIndex - leftPadding;
                        
                        if (kIndex - leftPadding <= 0) {
                            exitCol = 0;
                            hasLeftK = true;
                        }
                        
                        System.out.println("K found in additional line " + i + " at col " + exitCol);
                        break;
                    }
                }
            }
            
            // TAHAP 4: Periksa K di akhir baris (setelah ukuran normal board)
            if (!exitExists) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.length() > cols + leftPadding) {
                        int endIndex = cols + leftPadding;
                        if (endIndex < currentLine.length() && currentLine.charAt(endIndex) == 'K') {
                            exitExists = true;
                            exitRow = i;
                            exitCol = cols;
                            System.out.println("K found at end of row " + i + " after board width");
                            break;
                        }
                    }
                }
            }
            
            // Cari primary piece (P) dan tentukan orientasinya
            for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                String currentLine = boardLines.get(i);
                if (currentLine.length() <= leftPadding) continue;
                
                for (int j = leftPadding; j < currentLine.length(); j++) {
                    char c = currentLine.charAt(j);
                    
                    if (c == 'P') {
                        if (!foundPrimaryPiece) {
                            pRow = i;
                            pCol = j - leftPadding;
                            foundPrimaryPiece = true;
                        }
                        
                        // Periksa orientasi
                        if (j > leftPadding && currentLine.charAt(j-1) == 'P') isHorizontal = true;
                        if (j < currentLine.length()-1 && currentLine.charAt(j+1) == 'P') isHorizontal = true;
                    }
                }
            }
            
            // Periksa orientasi vertikal dari P jika belum terdeteksi horizontal
            if (foundPrimaryPiece && !isHorizontal) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    if (i == pRow) continue; // Skip baris primary piece
                    
                    String currentLine = boardLines.get(i);
                    int adjustedCol = pCol + leftPadding;
                    
                    if (adjustedCol < currentLine.length() && currentLine.charAt(adjustedCol) == 'P') {
                        isHorizontal = false; // Konfirmasi bahwa orientasi vertikal
                        break;
                    }
                }
            }
            
            // Log posisi piece dan exit yang ditemukan
            System.out.println("Primary piece found at [" + pRow + "," + pCol + "], Orientation: " + 
                            (isHorizontal ? "Horizontal" : "Vertical"));
            
            if (exitExists) {
                System.out.println("Exit found at [" + exitRow + "," + exitCol + "]");
                System.out.println("hasLeftK: " + hasLeftK + ", hasTopK: " + hasTopK);
            } else {
                System.out.println("No exit found, will add one automatically");
            }
            
            // Hitung dimensi board final
            int finalRows = rows;
            int finalCols = cols;

            // Jika K di baris tambahan di bawah, perluas baris
            if (exitExists && exitRow >= rows) {
                finalRows = exitRow + 1;
                System.out.println("Expanding rows for bottom exit: Final rows = " + finalRows);
            }

            // Jika K di akhir kolom, perluas kolom
            if (exitExists && exitCol >= cols) {
                finalCols = exitCol + 1;
                System.out.println("Expanding cols for right exit: Final cols = " + finalCols);
            }

            // Jika K di awal kolom (kolom 0), tambahkan kolom di kiri
            if (hasLeftK) {
                finalCols += 1;
                System.out.println("Adding column for left K: Final cols = " + finalCols);
            }

            // Jika tidak ada exit, tambahkan sesuai orientasi mobil
            if (!exitExists && foundPrimaryPiece) {
                if (isHorizontal) {
                    // Tambahkan exit di kanan untuk horizontal
                    exitRow = pRow;
                    exitCol = cols;
                    finalCols += 1;
                    exitExists = true;
                    System.out.println("Added automatic exit for horizontal at [" + exitRow + "," + exitCol + "]");
                } else {
                    // Tambahkan exit di bawah untuk vertikal
                    exitRow = rows;
                    exitCol = pCol;
                    finalRows += 1;
                    exitExists = true;
                    System.out.println("Added automatic exit for vertical at [" + exitRow + "," + exitCol + "]");
                }
            }

            // Buat board dengan dimensi final
            currentBoard = new char[finalRows][finalCols];

            // Isi dengan '.' dan perlakukan kasus hasTopK secara khusus
            if (hasTopK) {
                // Isi semua dengan '.' dulu
                for (int i = 0; i < finalRows; i++) {
                    for (int j = 0; j < finalCols; j++) {
                        currentBoard[i][j] = '.';
                    }
                }
                
                // Letakkan K di baris 0 pada kolom exitCol
                int adjustedExitCol = exitCol;
                if (hasLeftK) adjustedExitCol += 1; // Adjusting for hasLeftK
                currentBoard[0][adjustedExitCol] = 'K';
                System.out.println("Placed K at top row [0," + adjustedExitCol + "]");
                
                // Salin konten boardLines ke board, dimulai dari baris 1
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.length() <= leftPadding) continue;
                    
                    for (int j = leftPadding; j < currentLine.length(); j++) {
                        char c = currentLine.charAt(j);
                        if (c == ' ' || c == 'K') continue; // Skip spasi dan K
                        
                        // Hitung posisi kolom pada board
                        int destCol = j - leftPadding;
                        
                        // Pastikan koordinat valid untuk baris i+1 (karena baris 0 untuk K)
                        if (i+1 < finalRows && destCol >= 0 && destCol < finalCols) {
                            currentBoard[i+1][destCol] = c;
                        }
                    }
                }
            } else {
                // Untuk kasus non-hasTopK, gunakan kode yang sudah ada
                // Isi dengan '.'
                for (int i = 0; i < finalRows; i++) {
                    for (int j = 0; j < finalCols; j++) {
                        currentBoard[i][j] = '.';
                    }
                }
                
                // Salin konten dari input ke board
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.length() <= leftPadding) continue;
                    
                    for (int j = leftPadding; j < currentLine.length(); j++) {
                        char c = currentLine.charAt(j);
                        if (c == ' ') continue; // Skip spasi
                        
                        // Hitung posisi kolom pada board
                        int destCol = j - leftPadding;
                        
                        // Khusus untuk K di kolom pertama
                        if (c == 'K' && hasLeftK && j - leftPadding <= 0) {
                            destCol = 0;  // K di kolom 0
                        }
                        
                        // Pastikan koordinat valid
                        if (i >= 0 && i < finalRows && destCol >= 0 && destCol < finalCols) {
                            currentBoard[i][destCol] = c;
                        }
                    }
                }
                
                // Tambahkan K untuk kasus khusus non-hasTopK
                if (exitExists) {
                    // Untuk K di kolom pertama
                    if (hasLeftK) {
                        currentBoard[exitRow][0] = 'K';
                        System.out.println("Placed K at left [" + exitRow + ",0]");
                    }
                    
                    // Untuk K di baris tambahan di bawah
                    if (exitRow >= rows) {
                        int adjustedExitCol = exitCol;
                        if (hasLeftK) adjustedExitCol += 1;
                        
                        // Jika P vertikal, sejajarkan K dengan kolom P
                        if (!isHorizontal && foundPrimaryPiece) {
                            adjustedExitCol = pCol + (hasLeftK ? 1 : 0);
                        }
                        
                        if (exitRow >= 0 && exitRow < finalRows && adjustedExitCol >= 0 && adjustedExitCol < finalCols) {
                            currentBoard[exitRow][adjustedExitCol] = 'K';
                            System.out.println("Placed K at bottom [" + exitRow + "," + adjustedExitCol + "]");
                        }
                    }
                    
                    // Untuk K di akhir kolom
                    if (exitCol >= cols && !hasLeftK) {
                        int adjustedExitCol = finalCols - 1;
                        
                        if (exitRow >= 0 && exitRow < finalRows) {
                            currentBoard[exitRow][adjustedExitCol] = 'K';
                            System.out.println("Placed K at right [" + exitRow + "," + adjustedExitCol + "]");
                        }
                    }
                }
            }

            // Debug board setelah dimodifikasi
            System.out.println("Final board contents (" + finalRows + "x" + finalCols + "):");
            for (int i = 0; i < currentBoard.length; i++) {
                System.out.println(new String(currentBoard[i]));
            }
                        
            // Inisialisasi board pada GUI
            boardPane.updateBoard(currentBoard);
            
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