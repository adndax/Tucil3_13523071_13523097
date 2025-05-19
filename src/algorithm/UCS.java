package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class UCS {
    private static class Node {
        GameState state;
        double cost; // g(n) - number of moves
        Node parent;

        Node(GameState state, double cost, Node parent) {
            this.state = state;
            this.cost = cost;
            this.parent = parent;
        }
    }

    private int nodesVisited;
    private double executionTime;

    public GameState solve(Board board) {
        // Initialize priority queue with cost as priority
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Set<GameState> closedList = new HashSet<>();

        // Create initial state
        GameState initialState = new GameState(board);
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
                    Node successorNode = new Node(successor, currentNode.cost + 1, currentNode);
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

        Board currentBoard = solution.getBoard();
        List<Move> moves = solution.getMoves();

        // Print initial board
        System.out.println("Papan Awal:");
        currentBoard.printBoard(null);

        // Print each move and resulting board
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