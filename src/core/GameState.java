package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import algorithm.Heuristics;

public class GameState {
    private final Board board;
    private final List<Move> moves;
    private final double g; // Path cost (number of moves)
    private final double h; // Heuristic value
    private final double f; // Total cost (g + h for A*)
    private final String heuristicName; // Name of the heuristic being used

    // Constructor for initial state with default heuristic
    public GameState(Board board) {
        this(board, "manhattan");
    }

    // Constructor for initial state with specified heuristic
    public GameState(Board board, String heuristicName) {
        this.board = board;
        this.moves = new ArrayList<>();
        this.g = 0;
        this.heuristicName = heuristicName;
        this.h = heuristicName.equals("none") ? 0 : computeHeuristic(); // Skip heuristic for "none"
        this.f = g + h;
    }

    // Constructor for UCS (no heuristic calculation needed)
    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG, boolean isUCS) {
        this.board = board;
        this.moves = new ArrayList<>(parentMoves);
        this.moves.add(newMove);
        this.g = parentG + 1;
        this.heuristicName = "none";
        this.h = isUCS ? 0 : computeHeuristic(); // Skip heuristic calculation for UCS
        this.f = g + h;
    }

    // Constructor for successor state with default heuristic
    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG) {
        this(board, parentMoves, newMove, parentG, "manhattan");
    }

    // Constructor for successor state with specified heuristic
    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG, String heuristicName) {
        this.board = board;
        this.moves = new ArrayList<>(parentMoves);
        this.moves.add(newMove);
        this.g = parentG + 1;
        this.heuristicName = heuristicName;
        this.h = heuristicName.equals("none") ? 0 : computeHeuristic(); // Skip heuristic for "none"
        this.f = g + h;
    }

    // Compute heuristic based on the selected heuristic name
    private double computeHeuristic() {
        if (heuristicName == null || heuristicName.equals("none")) {
            return 0; // No heuristic for UCS
        } else if (heuristicName.equals("manhattan")) {
            // Use the original implementation for backward compatibility
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
        } else {
            // Use the Heuristics utility class for other heuristics
            return Heuristics.getHeuristic(board, heuristicName);
        }
    }

    // Get successor states
    public List<GameState> getSuccessors() {
        List<GameState> successors = new ArrayList<>();
        for (Move move : board.getAllPossibleMoves()) {
            Board newBoard = board.applyMove(move);
            // Use a special flag for UCS to avoid heuristic calculation
            boolean isUCS = heuristicName.equals("none");
            if (isUCS) {
                GameState successor = new GameState(newBoard, moves, move, g, true);
                successors.add(successor);
            } else {
                GameState successor = new GameState(newBoard, moves, move, g, heuristicName);
                successors.add(successor);
            }
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

    public String getHeuristicName() {
        return heuristicName;
    }

    public boolean isGoal() {
        return board.isSolved();
    }

    // Equality and hash code for state comparison
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameState))
            return false;
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