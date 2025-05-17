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
        for (int i = 0; i < rows; i++) {
            String line = reader.readLine().trim();
            for (int j = 0; j < cols; j++) {
                char c = line.charAt(j);
                grid[i][j] = c;
                if (c == 'K') {
                    exitRow = i;
                    exitCol = j;
                } else if (c != '.') {
                    pieceCells.computeIfAbsent(c, k -> new ArrayList<>()).add(new int[]{i, j});
                }
            }
        }
        reader.close();

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
            throw new IllegalArgumentException("Expected " + numNonPrimaryPieces + " non-primary pieces, found " + actualNonPrimary);
        }

        if (primaryPiece.isHorizontal() && exitRow != primaryPiece.getRow() ||
            !primaryPiece.isHorizontal() && exitCol != primaryPiece.getCol()) {
            throw new IllegalArgumentException("Exit not aligned with primary piece orientation");
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
            int exitColForPiece = primaryPiece.getCol() + primaryPiece.getSize() - 1;
            return primaryPiece.getRow() == exitRow && exitColForPiece == exitCol;
        } else {
            int exitRowForPiece = primaryPiece.getRow() + primaryPiece.getSize() - 1;
            return primaryPiece.getCol() == exitCol && exitRowForPiece == exitRow;
        }
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
        for (int[] cell : oldPiece.getOccupiedCells()) {
            newGrid[cell[0]][cell[1]] = '.';
        }
        for (int[] cell : newPiece.getOccupiedCells()) {
            newGrid[cell[0]][cell[1]] = newPiece.getId();
        }
        if (exitRow >= 0 && exitCol >= 0) {
            newGrid[exitRow][exitCol] = 'K';
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
                } else if (c == 'K') {
                    System.out.print("\u001B[32m" + c + "\u001B[0m ");
                } else if (c == movedPieceId && c != '.') {
                    System.out.print("\u001B[34m" + c + "\u001B[0m ");
                } else {
                    System.out.print(c + " ");
                }
            }
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
}