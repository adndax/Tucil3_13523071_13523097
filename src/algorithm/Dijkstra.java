package algorithm;

import core.Board;
import core.GameState;
import core.Move;
import utils.TimerUtil;

import java.util.*;

public class Dijkstra {
    private TimerUtil timer;
    private int nodesVisited;
    private Board initialBoard;
    
    public Dijkstra() {
        this.timer = new TimerUtil();
        this.nodesVisited = 0;
    }
    
    /**
     * Solves the Rush Hour puzzle using Dijkstra's algorithm.
     * Dijkstra is essentially UCS which finds the shortest path by considering only the path cost.
     * 
     * @param initialBoard The initial board configuration
     * @return GameState representing the solution, or null if no solution found
     */
    public GameState solve(Board initialBoard) {
        timer.start();
        nodesVisited = 0;
        this.initialBoard = initialBoard;
        
        // Priority queue ordered by path cost (g value)
        PriorityQueue<GameState> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(GameState::getG));
        
        // Set to track visited states
        Set<String> closedSet = new HashSet<>();
        
        // Map to store the best path cost to reach each state
        Map<String, Double> bestCost = new HashMap<>();
        
        // Initialize with start state
        GameState startState = new GameState(initialBoard);
        openSet.add(startState);
        
        String startStateKey = getBoardKey(startState.getBoard());
        bestCost.put(startStateKey, 0.0);
        
        while (!openSet.isEmpty()) {
            // Get state with lowest path cost
            GameState current = openSet.poll();
            nodesVisited++;
            
            String currentKey = getBoardKey(current.getBoard());
            
            // Check if goal is reached
            if (current.isGoal()) {
                timer.stop();
                return current;
            }
            
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
                
                double newCost = successor.getG(); // In Dijkstra, we only consider path cost
                
                // Check if this path to successor is better than any previous one
                if (!bestCost.containsKey(successorKey) || newCost < bestCost.get(successorKey)) {
                    bestCost.put(successorKey, newCost);
                    
                    // Only add to open set if it's not already there or if we found a better path
                    if (!isInOpenSet(openSet, successorKey)) {
                        openSet.add(successor);
                    } else {
                        // Remove the old one with worse cost and add the new one
                        updateOpenSet(openSet, successor, successorKey);
                    }
                }
            }
        }
        
        // No solution found
        timer.stop();
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
     * Updates a state in the open set with a better path
     */
    private void updateOpenSet(PriorityQueue<GameState> openSet, GameState newState, String key) {
        // Since PriorityQueue doesn't support direct element removal by value,
        // we have to remove all elements, update the one we're interested in, 
        // and add all back
        List<GameState> tempStates = new ArrayList<>();
        
        while (!openSet.isEmpty()) {
            GameState state = openSet.poll();
            if (!getBoardKey(state.getBoard()).equals(key)) {
                tempStates.add(state);
            }
        }
        
        // Add all states back plus the new one
        openSet.addAll(tempStates);
        openSet.add(newState);
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
            System.out.println("No solution found!");
            return;
        }
        
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
        
        // Verifikasi solusi
        if (!currentBoard.isSolved()) {
            System.out.println("Warning: Solusi mungkin belum lengkap. Primary piece belum mencapai exit point.");
        }
        
        System.out.println("Solusi ditemukan dalam " + moves.size() + " langkah");
        System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
        System.out.println("Waktu eksekusi: " + timer.getFormattedElapsedTime());
    }
    
    /**
     * Gets the number of nodes visited during search
     */
    public int getNodesVisited() {
        return nodesVisited;
    }
    
    /**
     * Gets the execution time
     */
    public String getExecutionTime() {
        return timer.getFormattedElapsedTime();
    }
    
    /**
     * Gets raw execution time in milliseconds
     */
    public double getExecutionTimeMs() {
        return timer.getElapsedTimeMs();
    }
}