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
    
    public GameState solve(Board initialBoard) {
        nodesVisited = 0;
        this.initialBoard = initialBoard;
        long startTime = System.nanoTime();
        
        PriorityQueue<GameState> openSet = new PriorityQueue<>(Comparator.comparingDouble(GameState::getF));
        
        Set<String> closedSet = new HashSet<>();
        
        Map<String, Double> bestGScore = new HashMap<>();
        
        GameState startState = new GameState(initialBoard, heuristicName);
        openSet.add(startState);
        
        String startStateKey = getBoardKey(startState.getBoard());
        bestGScore.put(startStateKey, 0.0);
        
        while (!openSet.isEmpty()) {
            GameState current = openSet.poll();
            nodesVisited++;
            
            if (current.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; 
                return current;
            }
            
            String currentKey = getBoardKey(current.getBoard());
            
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
                
                double tentativeG = successor.getG();
                
                if (!bestGScore.containsKey(successorKey) || tentativeG < bestGScore.get(successorKey)) {
                    bestGScore.put(successorKey, tentativeG);
                    
                    if (!isInOpenSet(openSet, successorKey)) {
                        openSet.add(successor);
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
    
    public String getHeuristicName() {
        return heuristicName;
    }
}