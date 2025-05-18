import java.io.IOException;
import java.util.List;
import core.Board;
import core.GameState;
import core.Move;
import core.Piece;

public class Main {
    public static void main(String[] args) {
        try {
            // Test 1: Board Initialization and Parsing
            System.out.println("=== Test 1: Board Initialization ===");
            Board board = new Board("test/test1.txt");
            System.out.println("Initial Board:");
            board.printBoard(null);
            System.out.println("Primary Piece: " + board.getPrimaryPiece().getId() +
                    " at (" + board.getPrimaryPiece().getRow() + "," + board.getPrimaryPiece().getCol() + ")");
            System.out.println("Exit at: (" + board.getExitRow() + "," + board.getExitCol() + ")");
            System.out.println("Is Solved? " + board.isSolved());

            // Test 2: Piece Move Generation
            System.out.println("\n=== Test 2: Piece Move Generation ===");
            for (Piece piece : board.getPieces()) {
                System.out.print("Piece " + piece.getId() + " possible moves: ");
                Move[] moves = piece.getPossibleMoves(board);
                for (Move move : moves) {
                    System.out.print(move + " ");
                }
                System.out.println();
            }

            // Test 3: Move Validation and Application
            System.out.println("\n=== Test 3: Move Application ===");
            List<Move> possibleMoves = board.getAllPossibleMoves();
            if (!possibleMoves.isEmpty()) {
                Move testMove = possibleMoves.get(0); // Pick first valid move
                System.out.println("Applying move: " + testMove);
                Board newBoard = board.applyMove(testMove);
                newBoard.printBoard(testMove);
                System.out.println("Is Solved? " + newBoard.isSolved());
            } else {
                System.out.println("No possible moves available.");
            }

            // Test 4: GameState Creation and Successors
            System.out.println("\n=== Test 4: GameState and Successors ===");
            GameState initialState = new GameState(board);
            System.out.println("Initial Heuristic (h): " + initialState.getH());
            System.out.println("Initial Path Cost (g): " + initialState.getG());
            System.out.println("Initial Total Cost (f): " + initialState.getF());
            List<GameState> successors = initialState.getSuccessors();
            System.out.println("Number of successors: " + successors.size());
            if (!successors.isEmpty()) {
                GameState successor = successors.get(0);
                Move move = successor.getMoves().get(0);
                System.out.println("First successor move: " + move);
                successor.getBoard().printBoard(move);
                System.out.println("Successor Heuristic (h): " + successor.getH());
            }

        } catch (IOException e) {
            System.out.println("Error loading board: " + e.getMessage());
        }
    }
}