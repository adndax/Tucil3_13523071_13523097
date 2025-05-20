package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class Dijkstra {
    private int nodesVisited;
    private double executionTime;
    private Board initialBoard;

    public Dijkstra() {
        this.nodesVisited = 0;
        this.executionTime = 0.0;
    }

    public GameState solve(Board initialBoard) {
        this.initialBoard = initialBoard;
        nodesVisited = 0;
        long startTime = System.nanoTime();
        
        PriorityQueue<GameState> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(GameState::getG));
        
        Set<String> closedSet = new HashSet<>();
        
        Map<String, Double> bestCost = new HashMap<>();
        
        GameState startState = new GameState(initialBoard);
        openSet.add(startState);
        
        String startStateKey = getBoardKey(startState.getBoard());
        bestCost.put(startStateKey, 0.0);
        
        while (!openSet.isEmpty()) {
            GameState current = openSet.poll();
            nodesVisited++;
            
            String currentKey = getBoardKey(current.getBoard());
            
            if (current.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; 
                return current;
            }
            
            if (closedSet.contains(currentKey)) {
                continue;
            }
            
            closedSet.add(currentKey);
            
            List<GameState> successors = current.getSuccessors();
            
            for (GameState successor : successors) {
                String successorKey = getBoardKey(successor.getBoard());
                
                if (closedSet.contains(successorKey)) {
                    continue;
                }
                
                double newCost = successor.getG();
                
                if (!bestCost.containsKey(successorKey) || newCost < bestCost.get(successorKey)) {
                    bestCost.put(successorKey, newCost);
                    
                    if (!isInOpenSet(openSet, successorKey)) {
                        openSet.add(successor);
                    } else {
                        updateOpenSet(openSet, successor, successorKey);
                    }
                }
            }
        }
        
        executionTime = (System.nanoTime() - startTime) / 1_000_000.0;
        return null;
    }
    
    private boolean isInOpenSet(PriorityQueue<GameState> openSet, String key) {
        for (GameState state : openSet) {
            if (getBoardKey(state.getBoard()).equals(key)) {
                return true;
            }
        }
        return false;
    }
    
    private void updateOpenSet(PriorityQueue<GameState> openSet, GameState newState, String key) {
        List<GameState> tempStates = new ArrayList<>();
        
        while (!openSet.isEmpty()) {
            GameState state = openSet.poll();
            if (!getBoardKey(state.getBoard()).equals(key)) {
                tempStates.add(state);
            }
        }
        
        openSet.addAll(tempStates);
        openSet.add(newState);
    }
    
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
        
        System.out.println("Papan Awal");
        initialBoard.printBoard(null); 
        
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
    
    public int getNodesVisited() {
        return nodesVisited;
    }
    
    public double getExecutionTime() {
        return executionTime;
    }
}