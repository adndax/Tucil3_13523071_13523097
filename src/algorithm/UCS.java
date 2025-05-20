package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class UCS {
    private static class Node {
        GameState state;
        double cost; // g(n) - number of moves

        Node(GameState state, double cost, Node parent) {
            this.state = state;
            this.cost = cost;
        }
    }

    private int nodesVisited;
    private double executionTime;

    private Board initialBoard; // Store the initial board

    public UCS() {
        this.nodesVisited = 0;
        this.executionTime = 0.0;
    }

    public GameState solve(Board board) {
        this.initialBoard = board; // Store the initial board for solution printing
        
        // Initialize priority queue with cost as priority
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Set<GameState> closedList = new HashSet<>();

        // Create initial state with no heuristic (UCS doesn't use heuristics)
        GameState initialState = new GameState(board, "none"); // Use the "none" heuristic
        Node initialNode = new Node(initialState, 0, null);
        openList.add(initialNode);

        nodesVisited = 0;
        long startTime = System.nanoTime();

        while (!openList.isEmpty()) {
            // Get node with lowest cost
            Node currentNode = openList.poll();
            GameState currentState = currentNode.state;
            nodesVisited++;

            // Check if goal state is reached
            if (currentState.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; // Convert to milliseconds
                return currentState; // No need to reconstruct the path, it's already in the GameState
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
                    // Use the cost from the successor's g value (path cost)
                    Node successorNode = new Node(successor, successor.getG(), currentNode);
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