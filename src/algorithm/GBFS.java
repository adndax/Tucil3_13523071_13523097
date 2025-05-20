package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class GBFS {
    private static class Node {
        GameState state;
        Node parent;

        Node(GameState state, Node parent) {
            this.state = state;
            this.parent = parent;
        }
    }

    private int nodesVisited;
    private double executionTime;
    private String heuristicName;
    private Board initialBoard;

    public GBFS(String heuristicName) {
        this.nodesVisited = 0;
        this.executionTime = 0.0;
        this.heuristicName = heuristicName;
    }

    public GameState solve(Board initialBoard) {
        this.initialBoard = initialBoard;
        nodesVisited = 0;
        long startTime = System.nanoTime();

        // Priority queue ordered by heuristic value (h)
        PriorityQueue<Node> openList = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.state.getH())
        );
        Set<String> closedList = new HashSet<>();

        // Initialize with start state
        GameState initialState = new GameState(initialBoard, heuristicName);
        Node initialNode = new Node(initialState, null);
        openList.add(initialNode);

        while (!openList.isEmpty()) {
            // Get node with lowest heuristic value
            Node currentNode = openList.poll();
            GameState currentState = currentNode.state;
            nodesVisited++;

            // Get unique key for current state
            String currentKey = getBoardKey(currentState.getBoard());

            // Check if goal is reached
            if (currentState.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; // Convert to milliseconds
                return currentState;
            }

            // Skip if state has been visited
            if (closedList.contains(currentKey)) {
                continue;
            }

            // Add to closed list
            closedList.add(currentKey);

            // Generate successors
            for (GameState successor : currentState.getSuccessors()) {
                String successorKey = getBoardKey(successor.getBoard());
                if (!closedList.contains(successorKey)) {
                    Node successorNode = new Node(successor, currentNode);
                    openList.add(successorNode);
                }
            }
        }

        // No solution found
        executionTime = (System.nanoTime() - startTime) / 1_000_000.0;
        return null;
    }

    /**
     * Creates a unique string key for a board state
     */
    private String getBoardKey(Board board) {
        StringBuilder key = new StringBuilder();
        char[][] grid = board.getGrid();
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                key.append(grid[i][j]);
            }
        }
        return key.toString();
    }

    public void printSolution(GameState solution) {
        if (solution == null) {
            System.out.println("Tidak ada solusi yang ditemukan!");
            return;
        }

        System.out.println("Menggunakan heuristic: " + heuristicName);
        
        List<Move> moves = solution.getMoves();

        // Print initial board
        System.out.println("Papan Awal:");
        initialBoard.printBoard(null);

        // Print each move and resulting board
        Board currentBoard = initialBoard;
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            currentBoard = currentBoard.applyMove(move);
            System.out.println("Gerakan " + (i + 1) + ": " + move);
            currentBoard.printBoard(move);
        }

        System.out.println("Solusi ditemukan dalam " + moves.size() + " langkah");
        System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
        System.out.println("Waktu eksekusi: " + executionTime + " ms");
    }

    /**
     * Gets the number of nodes visited during search
     */
    public int getNodesVisited() {
        return nodesVisited;
    }

    /**
     * Gets the execution time in milliseconds
     */
    public double getExecutionTime() {
        return executionTime;
    }
    
    /**
     * Gets the name of the heuristic being used
     */
    public String getHeuristicName() {
        return heuristicName;
    }
}