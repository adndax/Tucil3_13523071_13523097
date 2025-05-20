package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class AStar {
    private int nodesVisited;
    private double executionTime;
    private String heuristicName;
    private Board initialBoard; 
    
    public AStar(String heuristicName) {
        this.nodesVisited = 0;
        this.executionTime = 0.0;
        this.heuristicName = heuristicName;
    }
    
    /**
     * Solves the Rush Hour puzzle using A* algorithm.
     * 
     * @param initialBoard The initial board configuration
     * @return GameState representing the solution, or null if no solution found
     */
    public GameState solve(Board initialBoard) {
        nodesVisited = 0;
        this.initialBoard = initialBoard;
        long startTime = System.nanoTime();
        
        // Priority queue ordered by f value (g + h)
        PriorityQueue<GameState> openSet = new PriorityQueue<>(Comparator.comparingDouble(GameState::getF));
        
        // Set to track visited states
        Set<String> closedSet = new HashSet<>();
        
        // Map to store the best path cost to reach each state
        Map<String, Double> bestGScore = new HashMap<>();
        
        // Initialize with start state - pass the heuristic name
        GameState startState = new GameState(initialBoard, heuristicName);
        openSet.add(startState);
        
        String startStateKey = getBoardKey(startState.getBoard());
        bestGScore.put(startStateKey, 0.0);
        
        while (!openSet.isEmpty()) {
            // Get state with lowest f value
            GameState current = openSet.poll();
            nodesVisited++;
            
            // Check if goal is reached
            if (current.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; // Convert to milliseconds
                return current;
            }
            
            // Get unique key for current state
            String currentKey = getBoardKey(current.getBoard());
            
            // Skip if we've already processed this state with a better or equal path
            if (closedSet.contains(currentKey)) {
                continue;
            }
            
            // Add to closed set
            closedSet.add(currentKey);
            
            // Generate successors
            List<GameState> successors = current.getSuccessors();
            
            for (GameState successor : successors) {
                String successorKey = getBoardKey(successor.getBoard());
                
                // Skip if already processed
                if (closedSet.contains(successorKey)) {
                    continue;
                }
                
                double tentativeG = successor.getG();
                
                // Check if this path to successor is better than any previous one
                if (!bestGScore.containsKey(successorKey) || tentativeG < bestGScore.get(successorKey)) {
                    bestGScore.put(successorKey, tentativeG);
                    
                    // Only add to open set if it's not already there or if we found a better path
                    if (!isInOpenSet(openSet, successorKey)) {
                        openSet.add(successor);
                    }
                }
            }
        }
        
        // No solution found
        executionTime = (System.nanoTime() - startTime) / 1_000_000.0;
        return null;
    }
    
    /**
     * Checks if a state with the given key is in the open set
     */
    private boolean isInOpenSet(PriorityQueue<GameState> openSet, String key) {
        for (GameState state : openSet) {
            if (getBoardKey(state.getBoard()).equals(key)) {
                return true;
            }
        }
        return false;
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
    
    /**
     * Prints the solution path
     */
    public void printSolution(GameState solution) {
        if (solution == null) {
            System.out.println("Tidak ada solusi yang ditemukan!");
            return;
        }
        
        System.out.println("Menggunakan heuristic: " + heuristicName);
        
        List<Move> moves = solution.getMoves();
        
        // Print initial board state
        System.out.println("Papan Awal");
        initialBoard.printBoard(null); // No move to highlight for initial state
        
        // Print each move and resulting board state
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
     * Gets raw execution time in milliseconds
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