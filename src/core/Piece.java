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

    public Move[] getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        
        if (isHorizontal) {
            if (col > 0 && board.isCellEmpty(row, col - 1)) {
                moves.add(new Move(id, "kiri"));
            }
            if (col + size < board.getCols() && board.isCellEmpty(row, col + size)) {
                moves.add(new Move(id, "kanan"));
            }
        } else {
            if (row > 0 && board.isCellEmpty(row - 1, col)) {
                moves.add(new Move(id, "atas"));
            }
            if (row + size < board.getRows() && board.isCellEmpty(row + size, col)) {
                moves.add(new Move(id, "bawah"));
            }
        }
        return moves.toArray(new Move[0]);
    }

    public Piece applyMove(Move move) {
        String direction = move.getDirection();
        int newRow = row;
        int newCol = col;

        if (isHorizontal) {
            if (direction.equals("kiri")) {
                newCol--;
            } else if (direction.equals("kanan")) {
                newCol++;
            }
        } else {
            if (direction.equals("atas")) {
                newRow--;
            } else if (direction.equals("bawah")) {
                newRow++;
            }
        }

        return new Piece(id, newRow, newCol, size, isHorizontal, isPrimary);
    }

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