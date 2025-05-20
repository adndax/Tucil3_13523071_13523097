package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import algorithm.Heuristics;

public class GameState {
    private final Board board;
    private final List<Move> moves;
    private final double g; 
    private final double h; 
    private final double f; 
    private final String heuristicName; 

    public GameState(Board board) {
        this(board, "manhattan");
    }

    public GameState(Board board, String heuristicName) {
        this.board = board;
        this.moves = new ArrayList<>();
        this.g = 0;
        this.heuristicName = heuristicName;
        this.h = heuristicName.equals("none") ? 0 : computeHeuristic(); 
        this.f = g + h;
    }

    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG, boolean isUCS) {
        this.board = board;
        this.moves = new ArrayList<>(parentMoves);
        this.moves.add(newMove);
        this.g = parentG + 1;
        this.heuristicName = "none";
        this.h = isUCS ? 0 : computeHeuristic(); 
        this.f = g + h;
    }

    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG) {
        this(board, parentMoves, newMove, parentG, "manhattan");
    }

    public GameState(Board board, List<Move> parentMoves, Move newMove, double parentG, String heuristicName) {
        this.board = board;
        this.moves = new ArrayList<>(parentMoves);
        this.moves.add(newMove);
        this.g = parentG + 1;
        this.heuristicName = heuristicName;
        this.h = heuristicName.equals("none") ? 0 : computeHeuristic(); 
        this.f = g + h;
    }

    private double computeHeuristic() {
        if (heuristicName == null || heuristicName.equals("none")) {
            return 0;
        } else if (heuristicName.equals("manhattan")) {
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
            return Heuristics.getHeuristic(board, heuristicName);
        }
    }

    // Get successor states
    public List<GameState> getSuccessors() {
        List<GameState> successors = new ArrayList<>();
        for (Move move : board.getAllPossibleMoves()) {
            Board newBoard = board.applyMove(move);
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