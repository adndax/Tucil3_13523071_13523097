package core;

import java.util.ArrayList;
import java.util.List;

public class Piece {
    private char id;
    private int row;
    private int col;
    private int size;
    private boolean isHorizontal;
    private boolean isPrimary;

    public Piece(char id, int row, int col, int size, boolean isHorizontal, boolean isPrimary) {
        this.id = id;
        this.row = row;
        this.col = col;
        this.size = size;
        this.isHorizontal = isHorizontal;
        this.isPrimary = isPrimary;
    }

    public char getId() {
        return id;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getSize() {
        return size;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    // Mendapatkan semua pergerakan yang mungkin
    public Move[] getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        
        if (isHorizontal) {
            // Pergerakan ke kiri
            int maxStepsLeft = 0;
            for (int step = 1; col - step >= 0; step++) {
                if (board.isCellEmpty(row, col - step)) {
                    maxStepsLeft++;
                } else {
                    break;
                }
            }
            
            // Tambahkan pergerakan ke kiri untuk setiap langkah yang mungkin
            for (int step = 1; step <= maxStepsLeft; step++) {
                moves.add(new Move(id, "kiri", step));
            }
            
            // Pergerakan ke kanan
            int maxStepsRight = 0;
            for (int step = 1; col + size - 1 + step < board.getCols(); step++) {
                if (board.isCellEmpty(row, col + size - 1 + step)) {
                    maxStepsRight++;
                } else {
                    break;
                }
            }
            
            // Tambahkan pergerakan ke kanan untuk setiap langkah yang mungkin
            for (int step = 1; step <= maxStepsRight; step++) {
                moves.add(new Move(id, "kanan", step));
            }
            
        } else {
            // Pergerakan ke atas
            int maxStepsUp = 0;
            for (int step = 1; row - step >= 0; step++) {
                if (board.isCellEmpty(row - step, col)) {
                    maxStepsUp++;
                } else {
                    break;
                }
            }
            
            // Tambahkan pergerakan ke atas untuk setiap langkah yang mungkin
            for (int step = 1; step <= maxStepsUp; step++) {
                moves.add(new Move(id, "atas", step));
            }
            
            // Pergerakan ke bawah
            int maxStepsDown = 0;
            for (int step = 1; row + size - 1 + step < board.getRows(); step++) {
                if (board.isCellEmpty(row + size - 1 + step, col)) {
                    maxStepsDown++;
                } else {
                    break;
                }
            }
            
            // Tambahkan pergerakan ke bawah untuk setiap langkah yang mungkin
            for (int step = 1; step <= maxStepsDown; step++) {
                moves.add(new Move(id, "bawah", step));
            }
        }
        
        return moves.toArray(new Move[0]);
    }

    // Menerapkan gerakan
    public Piece applyMove(Move move) {
        String direction = move.getDirection();
        int steps = move.getSteps();
        int newRow = row;
        int newCol = col;

        if (isHorizontal) {
            if (direction.equals("kiri")) {
                newCol -= steps;
            } else if (direction.equals("kanan")) {
                newCol += steps;
            }
        } else {
            if (direction.equals("atas")) {
                newRow -= steps;
            } else if (direction.equals("bawah")) {
                newRow += steps;
            }
        }

        return new Piece(id, newRow, newCol, size, isHorizontal, isPrimary);
    }

    // Mendapatkan semua sel yang ditempati oleh piece ini
    public int[][] getOccupiedCells() {
        int[][] cells = new int[size][2];
        for (int i = 0; i < size; i++) {
            if (isHorizontal) {
                cells[i][0] = row;
                cells[i][1] = col + i;
            } else {
                cells[i][0] = row + i;
                cells[i][1] = col;
            }
        }
        return cells;
    }
}