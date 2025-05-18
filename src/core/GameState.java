package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState {
    private final Board board;
    private final List<Move> moves;
    private final double g; // Path cost (number of moves)
    private final double h; // Heuristic value (Manhattan distance)
    private final double f; // Total cost (g + h for A*)

    // Constructor for initial state
    public GameState(Board board) {
        this.board = board;
        this.moves = new ArrayList<>();
        this.g = 0;
        this.h = computeHeuristic();
        this.f = g + h;
    }

    // Constructor for successor state
    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG) {
        this.board = board;
        this.moves = new ArrayList<>(parentMoves);
        this.moves.add(newMove);
        this.g = parentG + 1;
        this.h = computeHeuristic();
        this.f = g + h;
    }

    // Compute Manhattan distance heuristic
    private double computeHeuristic() {
        Piece primary = board.getPrimaryPiece();
        int exitRow = board.getExitRow();
        int exitCol = board.getExitCol();

        if (primary.isHorizontal()) {
            int pieceEndCol = primary.getCol() + primary.getSize() - 1;
            return Math.abs(pieceEndCol - exitCol);
        } else {
            int pieceEndRow = primary.getRow() + primary.getSize() - 1;
            return Math.abs(pieceEndRow - exitRow);
        }
    }

    // Get successor states
    public List<GameState> getSuccessors() {
        List<GameState> successors = new ArrayList<>();
        for (Move move : board.getAllPossibleMoves()) {
            Board newBoard = board.applyMove(move);
            GameState successor = new GameState(newBoard, moves, move, g);
            successors.add(successor);
        }
        return successors;
    }

    // Getters
    public Board getBoard() {
        return board;
    }

    public List<Move> getMoves() {
        return Collections.unmodifiableList(moves);
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return f;
    }

    public boolean isGoal() {
        return board.isSolved();
    }

    // Equality and hash code for state comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameState)) return false;
        GameState other = (GameState) o;
        char[][] thisGrid = board.getGrid();
        char[][] otherGrid = other.board.getGrid();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                if (thisGrid[i][j] != otherGrid[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        char[][] grid = board.getGrid();
        int hash = 0;
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                hash = 31 * hash + grid[i][j];
            }
        }
        return hash;
    }
}