package algorithm;

import core.Board;
import core.GameState;
import core.Move;

import java.util.*;

public class UCS {
    private static class Node {
        GameState state;
        double cost; 

        Node(GameState state, double cost, Node parent) {
            this.state = state;
            this.cost = cost;
        }
    }

    private int nodesVisited;
    private double executionTime;

    private Board initialBoard;

    public UCS() {
        this.nodesVisited = 0;
        this.executionTime = 0.0;
    }

    public GameState solve(Board board) {
        this.initialBoard = board; 
        
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(n -> n.cost));
        Set<GameState> closedList = new HashSet<>();

        GameState initialState = new GameState(board, "none"); 
        Node initialNode = new Node(initialState, 0, null);
        openList.add(initialNode);

        nodesVisited = 0;
        long startTime = System.nanoTime();

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            GameState currentState = currentNode.state;
            nodesVisited++;

            if (currentState.isGoal()) {
                executionTime = (System.nanoTime() - startTime) / 1_000_000.0; 
                return currentState; 
            }

            if (closedList.contains(currentState)) {
                continue;
            }

            closedList.add(currentState);

            for (GameState successor : currentState.getSuccessors()) {
                if (!closedList.contains(successor)) {
                    Node successorNode = new Node(successor, successor.getG(), currentNode);
                    openList.add(successorNode);
                }
            }
        }

        executionTime = (System.nanoTime() - startTime) / 1_000_000.0;
        return null;
    }

    public void printSolution(GameState solution) {
        if (solution == null) {
            System.out.println("Tidak ada solusi yang ditemukan!");
            return;
        }

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
    }

    public int getNodesVisited() {
        return nodesVisited;
    }

    public double getExecutionTime() {
        return executionTime;
    }
}