package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class GBFS {
    private static class Node {
        GameState state;

        Node(GameState state, Node parent) {
            this.state = state;
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

        PriorityQueue<Node> openList = new PriorityQueue<>(
            Comparator.comparingDouble(n -> n.state.getH())
        );
        Set<String> closedList = new HashSet<>();

        GameState initialState = new GameState(initialBoard, heuristicName);
        Node initialNode = new Node(initialState, null);
        openList.add(initialNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            GameState currentState = currentNode.state;
            nodesVisited++;

            String currentKey = getBoardKey(currentState.getBoard());

            if (currentState.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; // Convert to milliseconds
                return currentState;
            }

            if (closedList.contains(currentKey)) {
                continue;
            }

            closedList.add(currentKey);

            for (GameState successor : currentState.getSuccessors()) {
                String successorKey = getBoardKey(successor.getBoard());
                if (!closedList.contains(successorKey)) {
                    Node successorNode = new Node(successor, currentNode);
                    openList.add(successorNode);
                }
            }
        }

        executionTime = (System.nanoTime() - startTime) / 1_000_000.0;
        return null;
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

        System.out.println("Papan Awal:");
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