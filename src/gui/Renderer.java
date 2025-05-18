package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Renderer {
    
    public interface StepChangeListener {
        void onStepChange(int stepIndex);
    }
    
    private List<StepChangeListener> stepChangeListeners = new ArrayList<>();
    
    public void addStepChangeListener(StepChangeListener listener) {
        stepChangeListeners.add(listener);
    }
    
    private BoardPane boardPane;
    private char[][] currentBoard;
    private List<MoveStep> solutionSteps;
    private int currentStepIndex = -1;
    private Timeline animation;
    
    private int totalMoves = 0;
    private int nodesVisited = 0;
    private long executionTime = 0;
    
    public Renderer(BoardPane boardPane) {
        this.boardPane = boardPane;
        this.solutionSteps = new ArrayList<>();
        setupAnimation();
    }
    
    private void setupAnimation() {
        animation = new Timeline(
            new KeyFrame(Duration.millis(1000), e -> showNextMove())
        );
        animation.setCycleCount(Timeline.INDEFINITE);
    }
    
    public void loadPuzzleFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] dimensions = reader.readLine().split(" ");
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);
            
            // Skip baris kedua yang berisi jumlah piece
            String secondLine = reader.readLine();
            boolean isNumericLine = false;
            
            try {
                Integer.parseInt(secondLine.trim());
                isNumericLine = true;
            } catch (NumberFormatException e) {
                // Bukan angka, jadi ini adalah baris pertama board
                isNumericLine = false;
            }
            
            currentBoard = new char[rows][cols];
            int currentRow = 0;
            
            // Jika baris kedua bukan angka, itu adalah baris pertama board
            if (!isNumericLine) {
                for (int j = 0; j < Math.min(cols, secondLine.length()); j++) {
                    currentBoard[currentRow][j] = secondLine.charAt(j);
                }
                currentRow++;
            }
            
            // Baca sisa baris board
            for (int i = currentRow; i < rows; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("File terlalu pendek, kurang baris");
                }
                for (int j = 0; j < Math.min(cols, line.length()); j++) {
                    currentBoard[i][j] = line.charAt(j);
                }
            }
            
            boardPane.initializeBoard(currentBoard);
            
            solutionSteps.clear();
            currentStepIndex = -1;
            
            solutionSteps.add(new MoveStep(copyBoard(currentBoard), null, null, 0));
            
            System.out.println("File berhasil dimuat. Board " + rows + "x" + cols);
            
        } catch (Exception e) {
            System.err.println("Error loading puzzle file: " + e.getMessage());
            e.printStackTrace();
        }
    }
        
    public boolean solvePuzzle(String algorithm, String heuristic) {
        long startTime = System.currentTimeMillis();
        
        // TODO: Replace with actual algorithm execution
        simulateSolution();
        
        executionTime = System.currentTimeMillis() - startTime;
        totalMoves = solutionSteps.size() - 1; 
        nodesVisited = totalMoves * 3; 
        
        return true;
    }
    
    private void simulateSolution() {
        // This is just a placeholder for the actual solution algorithm
        // In a real implementation, this would be replaced with actual algorithm
        
        // Simulated moves for testing
        char[][] board1 = copyBoard(currentBoard);
        movePieceOnBoard(board1, 'I', "left");
        solutionSteps.add(new MoveStep(board1, 'I', "left", 1));
        
        char[][] board2 = copyBoard(board1);
        movePieceOnBoard(board2, 'F', "down");
        solutionSteps.add(new MoveStep(board2, 'F', "down", 2));
        
        char[][] board3 = copyBoard(board2);
        movePieceOnBoard(board3, 'D', "up");
        solutionSteps.add(new MoveStep(board3, 'D', "up", 3));
        
        char[][] board4 = copyBoard(board3);
        movePieceOnBoard(board4, 'P', "right");
        solutionSteps.add(new MoveStep(board4, 'P', "right", 4));
    }
    
    private void movePieceOnBoard(char[][] board, char piece, String direction) {
        int pieceRow = -1, pieceCol = -1;
        int pieceEndRow = -1, pieceEndCol = -1;
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == piece) {
                    if (pieceRow == -1) {
                        pieceRow = i;
                        pieceCol = j;
                    }
                    pieceEndRow = i;
                    pieceEndCol = j;
                }
            }
        }
        
        if (pieceRow != -1 && pieceCol != -1) {
            boolean isHorizontal = pieceRow == pieceEndRow;
            
            switch (direction) {
                case "up":
                    if (!isHorizontal && pieceRow > 0 && 
                    (board[pieceRow - 1][pieceCol] == '.' || board[pieceRow - 1][pieceCol] == 'K')) {
                        board[pieceRow - 1][pieceCol] = piece;
                        board[pieceEndRow][pieceCol] = '.';
                    }
                    break;
                case "down":
                    if (!isHorizontal && pieceEndRow < board.length - 1 && 
                    (board[pieceEndRow + 1][pieceCol] == '.' || board[pieceEndRow + 1][pieceCol] == 'K')) {
                        board[pieceEndRow + 1][pieceCol] = piece;
                        board[pieceRow][pieceCol] = '.';
                    }
                    break;
                case "left":
                    if (isHorizontal && pieceCol > 0 && 
                    (board[pieceRow][pieceCol - 1] == '.' || board[pieceRow][pieceCol - 1] == 'K')) {
                        board[pieceRow][pieceCol - 1] = piece;
                        board[pieceRow][pieceEndCol] = '.';
                    }
                    break;
                case "right":
                    if (isHorizontal && pieceEndCol < board[0].length - 1 && 
                    (board[pieceRow][pieceEndCol + 1] == '.' || board[pieceRow][pieceEndCol + 1] == 'K')) {
                        board[pieceRow][pieceEndCol + 1] = piece;
                        board[pieceRow][pieceCol] = '.';
                    }
                    break;
            }
        }
    }
        
    public void showPreviousMove() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            displayMove(solutionSteps.get(currentStepIndex));
            
            notifyStepChangeListeners();
        }
    }
    
    public void showNextMove() {
        if (currentStepIndex < solutionSteps.size() - 1) {
            currentStepIndex++;
            displayMove(solutionSteps.get(currentStepIndex));
            
            notifyStepChangeListeners();
        } else {
            animation.stop();
        }
    }
    
    private void displayMove(MoveStep step) {
        boardPane.updateBoard(step.board);
        
        if (step.piece != null && step.direction != null) {
            boardPane.movePiece(step.piece, step.direction, 1);
        }
    }
    
    private void notifyStepChangeListeners() {
        for (StepChangeListener listener : stepChangeListeners) {
            listener.onStepChange(currentStepIndex);
        }
    }
    
    public void playAnimation() {
        currentStepIndex = 0;
        displayMove(solutionSteps.get(0));
        
        notifyStepChangeListeners();
        
        animation.play();
    }
    
    public void stopAnimation() {
        animation.stop();
    }
    
    public void jumpToStep(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < solutionSteps.size()) {
            currentStepIndex = stepIndex;
            displayMove(solutionSteps.get(currentStepIndex));
            
            notifyStepChangeListeners();
        }
    }
    
    private class MoveStep {
        char[][] board;
        Character piece;
        String direction;
        
        MoveStep(char[][] board, Character piece, String direction, int moveNumber) {
            this.board = board;
            this.piece = piece;
            this.direction = direction;
        }
    }
    
    private char[][] copyBoard(char[][] original) {
        if (original == null) return null;
        char[][] copy = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
    
    public int getTotalMoves() {
        return totalMoves;
    }
    
    public int getNodesVisited() {
        return nodesVisited;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void loadPuzzleFromFile(String filePath) {
        loadPuzzleFromFile(new File(filePath));
    }
    
    public int getCurrentStepIndex() {
        return currentStepIndex;
    }
    
    public int getTotalSteps() {
        return solutionSteps.size();
    }
}