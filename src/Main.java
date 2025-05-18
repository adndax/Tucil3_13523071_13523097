import java.io.IOException;
import java.util.List;
import core.Board;
import core.GameState;
import core.Move;
import core.Piece;
import algorithm.AStar;

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
            
            // Test 5: A* Algorithm Testing
            System.out.println("\n=== Test 5: A* Algorithm Testing ===");
            testAStar(board);

        } catch (IOException e) {
            System.out.println("Error loading board: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test the A* algorithm with different heuristics
     */
    private static void testAStar(Board board) {
        String[] heuristics = {"manhattan", "blocking", "combined"};
        
        // Test A* with different heuristics
        for (String heuristic : heuristics) {
            System.out.println("\n----- A* with " + heuristic + " heuristic -----");
            AStar astar = new AStar(heuristic);
            GameState astarSolution = astar.solve(board);
            System.out.println("Solution found: " + (astarSolution != null));
            if (astarSolution != null) {
                System.out.println("Solution steps: " + astarSolution.getMoves().size());
                System.out.println("Nodes visited: " + astar.getNodesVisited());
                System.out.println("Execution time: " + astar.getExecutionTime());
                
                System.out.println("\nSolution path:");
                astar.printSolution(astarSolution);
            }
        }
        
        // Print comparative summary for A*
        System.out.println("\n----- A* Comparison Summary -----");
        System.out.println("Heuristic\tSolution Steps\tNodes Visited\tExecution Time");
        
        for (String heuristic : heuristics) {
            AStar astar = new AStar(heuristic);
            GameState astarSolution = astar.solve(board);
            if (astarSolution != null) {
                System.out.println(heuristic + "\t\t" + astarSolution.getMoves().size() + "\t\t" + 
                                 astar.getNodesVisited() + "\t\t" + astar.getExecutionTime());
            } else {
                System.out.println(heuristic + "\t\tNo solution\t" + 
                                 astar.getNodesVisited() + "\t\t" + astar.getExecutionTime());
            }
        }
    }
    
    /**
     * Test with a different puzzle
     */
    private static void testWithAnotherPuzzle() {
        try {
            System.out.println("\n=== Testing with Another Puzzle ===");
            Board board = new Board("test/test2.txt");  // Make sure this file exists
            System.out.println("Initial Board:");
            board.printBoard(null);
            
            testAStar(board);
            
        } catch (IOException e) {
            System.out.println("Error loading second puzzle: " + e.getMessage());
        }
    }
}