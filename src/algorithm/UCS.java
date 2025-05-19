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

    public static List<Move> solve(Board board) {
        // Initialize priority queue with cost as priority
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Set<GameState> closedList = new HashSet<>();

        // Create initial state
        GameState initialState = new GameState(board);
        Node initialNode = new Node(initialState, 0, null);
        openList.add(initialNode);

        int nodesVisited = 0;

        long startTime = System.nanoTime();

        while (!openList.isEmpty()) {
            // Get node with lowest cost
            Node currentNode = openList.poll();
            GameState currentState = currentNode.state;
            nodesVisited++;

            // Check if goal state is reached
            if (currentState.isGoal()) {
                long endTime = System.nanoTime();
                double executionTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

                // Reconstruct path
                List<Move> solution = new ArrayList<>();
                Node node = currentNode;
                while (node.parent != null) {
                    solution.add(node.state.getMoves().get(node.state.getMoves().size() - 1));
                    node = node.parent;
                }
                Collections.reverse(solution);

                // Print results
                System.out.println("Jumlah node yang diperiksa: " + nodesVisited);
                System.out.printf("Waktu eksekusi: %.2f ms%n", executionTime);

                // Print board states
                Board currentBoard = board;
                System.out.println("Papan Awal:");
                currentBoard.printBoard(null);
                for (int i = 0; i < solution.size(); i++) {
                    Move move = solution.get(i);
                    currentBoard = currentBoard.applyMove(move);
                    System.out.println("Gerakan " + (i + 1) + ": " + move);
                    currentBoard.printBoard(move);
                }

                return solution;
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
        System.out.println("Tidak ada solusi yang ditemukan!");
        return null;
    }
}