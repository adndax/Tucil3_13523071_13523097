package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    private int rows;
    private int cols;
    private char[][] grid;
    private List<Piece> pieces;
    private Piece primaryPiece;
    private int exitRow;
    private int exitCol;

    public Board(String filename) throws IOException {
        pieces = new ArrayList<>();
        exitRow = -1; // Initialize to -1 to indicate no exit found yet
        exitCol = -1;
        readBoardFromFile(filename);
    }

    public Board(int rows, int cols, char[][] grid, List<Piece> pieces, Piece primaryPiece, int exitRow, int exitCol) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, this.grid[i], 0, cols);
        }
        this.pieces = new ArrayList<>();
        for (Piece p : pieces) {
            this.pieces.add(new Piece(p.getId(), p.getRow(), p.getCol(), p.getSize(), p.isHorizontal(), p.isPrimary()));
        }
        this.primaryPiece = new Piece(primaryPiece.getId(), primaryPiece.getRow(), primaryPiece.getCol(),
                primaryPiece.getSize(), primaryPiece.isHorizontal(), primaryPiece.isPrimary());
        this.exitRow = exitRow;
        this.exitCol = exitCol;
    }

    private void readBoardFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String[] dimensions = reader.readLine().trim().split("\\s+");
        rows = Integer.parseInt(dimensions[0]);
        cols = Integer.parseInt(dimensions[1]);
        grid = new char[rows][cols];

        int numNonPrimaryPieces = Integer.parseInt(reader.readLine().trim());

        Map<Character, List<int[]>> pieceCells = new HashMap<>();

        // Variabel untuk menghitung jumlah K
        int kCount = 0;

        // Baca seluruh file untuk menghitung jumlah K
        List<String> allLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            allLines.add(line);
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == 'K') {
                    kCount++;
                }
            }
        }

        // Cek jumlah K (pintu keluar)
        if (kCount == 0) {
            throw new IllegalArgumentException("No exit point found");
        } else if (kCount > 1) {
            System.out.println("Debug: Found " + kCount + " exit points ('K'). Only one exit is allowed.");
            throw new IllegalArgumentException("Multiple exit points found");
        }

        // Lanjutkan dengan membaca grid dan memproses pieces
        // Reset reader untuk membaca dari awal
        reader.close();
        reader = new BufferedReader(new FileReader(filename));

        // Skip baris dimensi dan jumlah non-primary pieces
        reader.readLine();
        reader.readLine();

        // Baca baris-baris grid
        for (int i = 0; i < rows; i++) {
            line = i < allLines.size() ? allLines.get(i) : "";
            line = line.trim();

            // Validasi panjang baris
            if (line.length() < cols) {
                throw new IllegalArgumentException("Line too short at row " + i);
            }

            // Periksa apakah pintu keluar ada di baris ini (tepat di luar grid)
            if (line.length() > cols && line.charAt(cols) == 'K') {
                exitRow = i;
                exitCol = cols; // Pintu keluar di kanan grid
                System.out
                        .println("Debug: Found exit at row " + exitRow + ", col " + exitCol + " (outside right grid)");
            }

            // Proses grid utama
            for (int j = 0; j < cols; j++) {
                char c = j < line.length() ? line.charAt(j) : '.';
                if (c == 'K') {
                    // Jika 'K' ada di dalam grid
                    exitRow = i;
                    exitCol = j;
                    grid[i][j] = '.'; // Kosongkan sel
                    System.out.println("Debug: Found exit at row " + exitRow + ", col " + exitCol + " (inside grid)");
                } else {
                    grid[i][j] = c;
                    if (c != '.') {
                        pieceCells.computeIfAbsent(c, k -> new ArrayList<>()).add(new int[] { i, j });
                    }
                }
            }
        }

        // Periksa baris tambahan (jika ada)
        if (rows < allLines.size()) {
            line = allLines.get(rows);
            int kIndex = line.indexOf('K');
            if (kIndex != -1) {
                exitRow = rows; // Pintu keluar di bawah grid
                exitCol = kIndex;
                System.out.println("Debug: Found exit at row " + exitRow + ", col " + exitCol + " (below grid)");
            }
        }

        // Pastikan pintu keluar ditemukan (sudah divalidasi sebelumnya dengan kCount)
        if (exitRow == -1 || exitCol == -1) {
            throw new IllegalArgumentException("No exit point found");
        }

        // Proses dan validasi pieces
        for (Map.Entry<Character, List<int[]>> entry : pieceCells.entrySet()) {
            char id = entry.getKey();
            List<int[]> cells = entry.getValue();
            if (cells.size() < 2 || cells.size() > 3) {
                throw new IllegalArgumentException("Invalid piece size for piece " + id);
            }
            int size = cells.size();
            int minRow = cells.get(0)[0];
            int minCol = cells.get(0)[1];
            boolean isHorizontal = cells.get(0)[0] == cells.get(1)[0];
            for (int[] cell : cells) {
                minRow = Math.min(minRow, cell[0]);
                minCol = Math.min(minCol, cell[1]);
            }
            boolean isPrimary = (id == 'P');
            Piece piece = new Piece(id, minRow, minCol, size, isHorizontal, isPrimary);
            pieces.add(piece);
            if (isPrimary) {
                primaryPiece = piece;
            }
        }

        if (primaryPiece == null) {
            throw new IllegalArgumentException("No primary piece found");
        }

        int actualNonPrimary = pieces.size() - 1;
        if (actualNonPrimary != numNonPrimaryPieces) {
            throw new IllegalArgumentException(
                    "Expected " + numNonPrimaryPieces + " non-primary pieces, found " + actualNonPrimary);
        }

        // Debug primary piece dan pintu keluar
        System.out.println("Debug: Primary piece at col " + primaryPiece.getCol() + ", row " + primaryPiece.getRow() +
                ", isHorizontal: " + primaryPiece.isHorizontal());
        System.out.println("Debug: Exit at col " + exitCol + ", row " + exitRow);

        // Validasi alignment pintu keluar dengan mobil utama
        if (primaryPiece.isHorizontal()) {
            // Untuk mobil horizontal, pintu keluar harus berada di baris yang sama
            if (exitRow != primaryPiece.getRow()) {
                System.out.println("Debug: Exit and primary piece not aligned horizontally");
                System.out.println("Debug: Exit row: " + exitRow + ", Primary piece row: " + primaryPiece.getRow());
                throw new IllegalArgumentException("Exit not aligned with horizontal primary piece");
            }
        } else {
            // Untuk mobil vertikal, pintu keluar harus berada di kolom yang sama
            if (exitCol != primaryPiece.getCol()) {
                System.out.println("Debug: Exit and primary piece not aligned vertically");
                System.out.println("Debug: Exit col: " + exitCol + ", Primary piece col: " + primaryPiece.getCol());
                throw new IllegalArgumentException("Exit not aligned with vertical primary piece");
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Piece getPrimaryPiece() {
        return primaryPiece;
    }

    public int getExitRow() {
        return exitRow;
    }

    public int getExitCol() {
        return exitCol;
    }

    public boolean isCellEmpty(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return false;
        }
        return grid[row][col] == '.' || grid[row][col] == 'K';
    }

    public boolean isSolved() {
        if (primaryPiece.isHorizontal()) {
            if (exitCol == cols) {
                // Pintu keluar di kanan grid
                // Pastikan primary piece berada tepat di samping exit point
                return primaryPiece.getRow() == exitRow &&
                        primaryPiece.getCol() + primaryPiece.getSize() == cols;
            } else if (exitCol < cols) {
                // Pintu keluar di dalam grid
                // Pastikan primary piece berada tepat di atas exit point
                return primaryPiece.getRow() == exitRow &&
                        primaryPiece.getCol() + primaryPiece.getSize() == exitCol + 1;
            }
        } else {
            if (exitRow == rows) {
                // Pintu keluar di bawah grid
                // Pastikan primary piece berada tepat di atas exit point
                return primaryPiece.getCol() == exitCol &&
                        primaryPiece.getRow() + primaryPiece.getSize() == rows;
            } else if (exitRow < rows) {
                // Pintu keluar di dalam grid
                // Pastikan primary piece berada tepat di atas exit point
                return primaryPiece.getCol() == exitCol &&
                        primaryPiece.getRow() + primaryPiece.getSize() == exitRow + 1;
            }
        }
        return false;
    }

    public List<Move> getAllPossibleMoves() {
        List<Move> moves = new ArrayList<>();
        for (Piece piece : pieces) {
            for (Move move : piece.getPossibleMoves(this)) {
                moves.add(move);
            }
        }
        return moves;
    }

    public Board applyMove(Move move) {
        char pieceId = move.getPieceId();
        Piece oldPiece = null;
        for (Piece p : pieces) {
            if (p.getId() == pieceId) {
                oldPiece = p;
                break;
            }
        }
        if (oldPiece == null) {
            throw new IllegalArgumentException("Piece not found: " + pieceId);
        }

        Piece newPiece = oldPiece.applyMove(move);
        List<Piece> newPieces = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getId() == pieceId) {
                newPieces.add(newPiece);
            } else {
                newPieces.add(p);
            }
        }

        char[][] newGrid = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, newGrid[i], 0, cols);
        }

        // Kosongkan sel-sel yang sebelumnya ditempati oleh piece yang digerakkan
        for (int[] cell : oldPiece.getOccupiedCells()) {
            if (cell[0] >= 0 && cell[0] < rows && cell[1] >= 0 && cell[1] < cols) {
                newGrid[cell[0]][cell[1]] = '.';
            }
        }

        // Tempati sel-sel baru dengan piece yang digerakkan
        for (int[] cell : newPiece.getOccupiedCells()) {
            if (cell[0] >= 0 && cell[0] < rows && cell[1] >= 0 && cell[1] < cols) {
                newGrid[cell[0]][cell[1]] = newPiece.getId();
            }
        }

        Piece newPrimaryPiece = primaryPiece.getId() == pieceId ? newPiece : primaryPiece;
        return new Board(rows, cols, newGrid, newPieces, newPrimaryPiece, exitRow, exitCol);
    }

    public void printBoard(Move move) {
        char movedPieceId = move != null ? move.getPieceId() : 0;
        System.out.println("Papan:");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = grid[i][j];
                if (c == 'P') {
                    System.out.print("\u001B[31m" + c + "\u001B[0m ");
                } else if (c == movedPieceId && c != '.') {
                    System.out.print("\u001B[34m" + c + "\u001B[0m ");
                } else {
                    System.out.print(c + " ");
                }
            }

            // Tampilkan pintu keluar jika berada di baris ini dan tepat di luar grid
            if (i == exitRow && exitCol == cols) {
                System.out.print("\u001B[32mK\u001B[0m");
            }

            System.out.println();
        }

        // Tampilkan pintu keluar jika berada di bawah grid
        if (exitRow == rows) {
            for (int j = 0; j < exitCol; j++) {
                System.out.print("  "); // Dua spasi untuk setiap kolom sebelum 'K'
            }
            System.out.print("\u001B[32mK\u001B[0m");
            System.out.println();
        }

        System.out.println();
    }

    public char[][] getGrid() {
        char[][] copy = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    public List<Piece> getPieces() {
        List<Piece> copy = new ArrayList<>();
        for (Piece p : pieces) {
            copy.add(new Piece(p.getId(), p.getRow(), p.getCol(), p.getSize(), p.isHorizontal(), p.isPrimary()));
        }
        return copy;
    }
    
    public Board reverseMove(Move move) {
        // For a move, we need to create its opposite
        char pieceId = move.getPieceId();
        String direction = move.getDirection();

        // Create opposite direction
        String oppositeDirection;
        switch (direction) {
            case "up":
                oppositeDirection = "down";
                break;
            case "down":
                oppositeDirection = "up";
                break;
            case "left":
                oppositeDirection = "right";
                break;
            case "right":
                oppositeDirection = "left";
                break;
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }

        // Create the reverse move
        Move reverseMove = new Move(pieceId, oppositeDirection);

        // Apply the reverse move
        return applyMove(reverseMove);
    }
}