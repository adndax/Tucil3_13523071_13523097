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
        Set<GameState> closedList = new HashSet<>();

        // Initialize with start state
        GameState initialState = new GameState(initialBoard, heuristicName);
        Node initialNode = new Node(initialState, null);
        openList.add(initialNode);

        while (!openList.isEmpty()) {
            // Get node with lowest heuristic value
            Node currentNode = openList.poll();
            GameState currentState = currentNode.state;
            nodesVisited++;

            // Check if goal is reached
            if (currentState.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; // Convert to milliseconds
                return currentState;
            }

            // Skip if state has been visited
            if (closedList.contains(currentState)) {
                continue;
            }

            // Add to closed list
            closedList.add(currentState);

            // Generate successors
            for (GameState successor : currentState.getSuccessors()) {
                if (!closedList.contains(successor)) {
                    Node successorNode = new Node(successor, currentNode);
                    openList.add(successorNode);
                }
            }
        }

        // No solution found
        executionTime = (System.nanoTime() - startTime) / 1_000_000.0;
        return null;
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
    }

    public int getNodesVisited() {
        return nodesVisited;
    }

    public double getExecutionTime() {
        return executionTime;
    }
}