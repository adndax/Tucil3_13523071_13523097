package gui;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class BoardPane extends GridPane {
    
    // TODO: pintu keluar masih salah HUAAAA gamuncul
    private int rows = 6;
    private int cols = 6;
    private double cellSize = 60;
    private Map<Character, StackPane> pieceMap;
    private char[][] board;
    
    private int exitRow = -1;
    private int exitCol = -1;
    
    private int gridRows;
    private int gridCols;
    
    public BoardPane() {
        this.getStyleClass().add("board-pane");
        pieceMap = new HashMap<>();
        
        setHgap(2);
        setVgap(2);
        setPadding(new Insets(15));
        
        this.setMaxWidth((cols + 1) * (cellSize + 2) + 30);
        this.setMaxHeight((rows + 1) * (cellSize + 2) + 30);
        
        System.out.println("Working directory: " + System.getProperty("user.dir"));
    }
    
    public void initializeBoard(char[][] board) {
        this.board = board;
        this.rows = board.length;
        this.cols = board[0].length;
        
        this.getChildren().clear();
        pieceMap.clear();
        
        findExitPosition();
        
        calculateGridDimensions();
        
        System.out.println("Board contents:");
        for (int i = 0; i < board.length; i++) {
            System.out.println(new String(board[i]));
        }
        System.out.println("Exit position: row=" + exitRow + ", col=" + exitCol);
        System.out.println("Grid dimensions: rows=" + gridRows + ", cols=" + gridCols);
        
        createEmptyCells();
        placePieces();
        
        if (exitRow >= 0 && exitCol >= 0) {
            placeExitDoor();
        }
    }
    
    private void findExitPosition() {
        exitRow = -1;
        exitCol = -1;
        
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == 'K') {
                    exitRow = row;
                    exitCol = col;
                    return;
                }
            }
        }
    }
    
    private void calculateGridDimensions() {
        gridRows = rows;
        gridCols = cols;
        
        if (exitRow >= 0) {
            if (exitRow >= rows) {
                gridRows = exitRow + 1;
            }
        }
        
        if (exitCol >= 0) {
            if (exitCol >= cols) {
                gridCols = exitCol + 1;
            }
        }
    }
    
    private void createEmptyCells() {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.getStyleClass().add("rectangle-cell");
                cell.setFill(Color.web("#f5f5f5"));
                cell.setStroke(Color.web("#e0e0e0"));
                cell.setStrokeWidth(1);
                cell.setArcWidth(5);
                cell.setArcHeight(5);
                add(cell, col, row);
            }
        }
    }
    
    private void placePieces() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (row < board.length && col < board[row].length) {
                    char piece = board[row][col];
                    if (piece != '.' && piece != 'K') {
                        placePiece(piece, row, col);
                    } else if (piece == '.') {
                        Text dotText = new Text(".");
                        dotText.setFill(Color.LIGHTGRAY);
                        dotText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                        add(dotText, col, row);
                    }
                }
            }
        }
    }
    
    private void placeExitDoor() {
        StackPane exitNode = createExitNode();
        add(exitNode, exitCol, exitRow);
        System.out.println("Placed EXIT door at row=" + exitRow + ", col=" + exitCol);
        
        pieceMap.put('K', exitNode);
    }
    
    private void placePiece(char piece, int row, int col) {
        if (!pieceMap.containsKey(piece)) {
            StackPane pieceNode = createPieceNode(piece);
            add(pieceNode, col, row);
            
            pieceMap.put(piece, pieceNode);
            
            if (isHorizontalPiece(piece, row, col)) {
                int size = getPieceSize(piece, row, col, true);
                Rectangle pieceRect = (Rectangle) pieceNode.getChildren().get(0);
                pieceRect.setWidth(cellSize * size + (size - 1) * getHgap());
                GridPane.setColumnSpan(pieceNode, size);
            } else if (isVerticalPiece(piece, row, col)) {
                int size = getPieceSize(piece, row, col, false);
                Rectangle pieceRect = (Rectangle) pieceNode.getChildren().get(0);
                pieceRect.setHeight(cellSize * size + (size - 1) * getVgap());
                GridPane.setRowSpan(pieceNode, size);
            }
        }
    }
    
    private StackPane createPieceNode(char piece) {
        StackPane piecePane = new StackPane();
        Rectangle rect = createPieceRectangle(piece);
        
        Text pieceText = new Text(String.valueOf(piece));
        pieceText.setFill(Color.WHITE);
        pieceText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        piecePane.getChildren().addAll(rect, pieceText);
        return piecePane;
    }

    private StackPane createExitNode() {
        StackPane exitPane = new StackPane();
        Rectangle exitRect = new Rectangle(cellSize, cellSize);
        exitRect.getStyleClass().add("exit-door");
        
        exitRect.setFill(Color.web("#4CAF50"));
        exitRect.setStroke(Color.web("#388E3C"));
        exitRect.setStrokeWidth(2);
        exitRect.setArcWidth(10);
        exitRect.setArcHeight(10);
        
        Text exitLetter = new Text("K");
        exitLetter.setFill(Color.WHITE);
        exitLetter.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        exitPane.getChildren().addAll(exitRect, exitLetter);
        return exitPane;
    }
    
    private Rectangle createPieceRectangle(char piece) {
        Rectangle rect = new Rectangle(cellSize, cellSize);
        
        if (piece == 'P') {
            rect.getStyleClass().add("primary-piece");
            rect.setFill(Color.web("#F25E59"));
        } else {
            rect.getStyleClass().add("piece");
            rect.getStyleClass().add("piece-" + piece);
            
            String[] colorPalette = {
                "#FAD390", "#F8C291", "#6A89CC", "#82CCDD", 
                "#B8E994", "#F6B93B", "#78E08F", "#E55039",
                "#fa8231", "#f7b731", "#BDC581", "#A3CB38"
            };
            int colorIndex = (piece - 'A') % colorPalette.length;
            if (colorIndex < 0) colorIndex += colorPalette.length;
            rect.setFill(Color.web(colorPalette[colorIndex]));
        }
        
        rect.setStroke(Color.web("#725861"));
        rect.setStrokeWidth(1.5);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        
        return rect;
    }
    
    private boolean isHorizontalPiece(char piece, int row, int col) {
        return col + 1 < cols && row < board.length && col + 1 < board[row].length && board[row][col + 1] == piece;
    }
    
    private boolean isVerticalPiece(char piece, int row, int col) {
        return row + 1 < rows && row + 1 < board.length && col < board[row + 1].length && board[row + 1][col] == piece;
    }
    
    private int getPieceSize(char piece, int row, int col, boolean horizontal) {
        int size = 1;
        if (horizontal) {
            for (int c = col + 1; c < cols && c < board[row].length && board[row][c] == piece; c++) {
                size++;
            }
        } else {
            for (int r = row + 1; r < rows && r < board.length && col < board[r].length && board[r][col] == piece; r++) {
                size++;
            }
        }
        return size;
    }
    
    public void movePiece(char piece, String direction, int steps) {
        if (!pieceMap.containsKey(piece)) return;
        
        StackPane pieceNode = pieceMap.get(piece);
        TranslateTransition transition = new TranslateTransition(Duration.millis(500), pieceNode);
        
        switch (direction) {
            case "up":
                transition.setByY(-(cellSize + getVgap()) * steps);
                break;
            case "down":
                transition.setByY((cellSize + getVgap()) * steps);
                break;
            case "left":
                transition.setByX(-(cellSize + getHgap()) * steps);
                break;
            case "right":
                transition.setByX((cellSize + getHgap()) * steps);
                break;
        }
        
        transition.play();
    }
    
    public void movePiece(Character piece, String direction, int steps) {
        movePiece((char)piece, direction, steps);
    }
    
    public void updateBoard(char[][] newBoard) {
        this.board = newBoard;
        this.getChildren().clear();
        pieceMap.clear();
        initializeBoard(newBoard);
    }
}