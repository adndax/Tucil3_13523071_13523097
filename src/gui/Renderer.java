package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import algorithm.AStar;
import algorithm.Dijkstra;
import algorithm.GBFS;
import algorithm.UCS;
import core.Board;
import core.GameState;
import core.Move;

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

    private String lastUsedAlgorithm = null;
    private String lastUsedHeuristic = null;
    
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
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            
            // Parse dimensions
            String dimensionsLine = reader.readLine();
            if (dimensionsLine == null) {
                throw new IllegalArgumentException("File is empty or cannot be read.");
            }
            
            String[] dimensions = dimensionsLine.split("\\s+");
            if (dimensions.length < 2) {
                throw new IllegalArgumentException("Invalid dimensions format");
            }
            
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);
            
            System.out.println("Loading board with dimensions: " + rows + "x" + cols);
            
            String secondLine = reader.readLine();
            boolean isNumericLine = true;
            
            try {
                Integer.parseInt(secondLine.trim());
            } catch (NumberFormatException e) {
                isNumericLine = false;
            }
            
            List<String> boardLines = new ArrayList<>();
            String line;
            int leftPadding = 0;
            boolean hasLeftK = false;
            boolean hasTopK = false;
            boolean exitExists = false;
            int exitRow = -1, exitCol = -1;
            int kCount = 0; 

            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == 'K') {
                        kCount++;
                    }
                }
                
                String trimmed = line.trim();
                if (boardLines.isEmpty() && (trimmed.equals("K") || (trimmed.length() > 1 && trimmed.replace("K", "").trim().isEmpty()))) {
                    hasTopK = true;
                    exitCol = line.indexOf('K');
                    System.out.println("K found in top row at col: " + exitCol);
                    continue;
                }
                boardLines.add(line);
            }
            
            if (kCount == 0) {
                throw new IllegalArgumentException("Error: No exit (K) found in the puzzle.");
            } else if (kCount > 1) {
                throw new IllegalArgumentException("Error: Multiple exits (K) found. Only one exit is allowed.");
            }
            
            int minLeadingSpace = Integer.MAX_VALUE;
            for (String boardLine : boardLines) {
                if (boardLine.trim().isEmpty()) continue;
                int leadingSpaces = boardLine.indexOf(boardLine.trim().charAt(0));
                minLeadingSpace = Math.min(minLeadingSpace, leadingSpaces);
            }
            if (minLeadingSpace == Integer.MAX_VALUE) minLeadingSpace = 0;
            leftPadding = minLeadingSpace;
            
            System.out.println("Minimum leading spaces: " + leftPadding);
            
            boolean foundPrimaryPiece = false;
            boolean isHorizontal = false;
            int pRow = -1, pCol = -1;
            int pCount = 0; 
            
            for (int i = 0; i < boardLines.size(); i++) {
                String currentLine = boardLines.get(i);
                for (int j = 0; j < currentLine.length(); j++) {
                    if (j < currentLine.length() && currentLine.charAt(j) == 'P') {
                        pCount++;
                        if (!foundPrimaryPiece) {
                            pRow = i;
                            pCol = j - leftPadding;
                            foundPrimaryPiece = true;
                        }
                    }
                }
            }
            
            if (pCount == 0) {
                throw new IllegalArgumentException("Error: No primary piece (P) found in the puzzle.");
            }
            
            if (boardLines.size() > 0) {
                String firstLine = boardLines.get(0);
                if (firstLine.contains("K") && firstLine.indexOf('K') <= firstLine.length() - 1) {
                    exitExists = true;
                    exitRow = 0;
                    exitCol = firstLine.indexOf('K') - leftPadding;
                    hasTopK = true;
                    System.out.println("K found in first row at col " + exitCol);
                }
            }
            
            if (!exitExists) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.trim().startsWith("K")) {
                        exitExists = true;
                        exitRow = i;
                        
                        int kPos = currentLine.indexOf('K');
                        if (kPos - leftPadding <= 0) {
                            exitCol = 0;
                            hasLeftK = true;
                            System.out.println("K found at start of row " + i + " with left padding " + leftPadding);
                        } else {
                            exitCol = kPos - leftPadding;
                            System.out.println("K found in row " + i + " at col " + exitCol);
                        }
                        break;
                    }
                }
            }
            
            if (!exitExists) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    int kIndex = currentLine.indexOf('K');
                    if (kIndex != -1) {
                        exitExists = true;
                        exitRow = i;
                        exitCol = kIndex - leftPadding;
                        
                        if (kIndex - leftPadding <= 0) {
                            hasLeftK = true;
                            exitCol = 0;
                        }
                        
                        System.out.println("K found in board at row " + i + ", col " + exitCol);
                        break;
                    }
                }
            }
            
            if (!exitExists && boardLines.size() > rows) {

                for (int i = rows; i < boardLines.size(); i++) {
                    String currentLine = boardLines.get(i);
                    String trimmed = currentLine.trim();
                    if (trimmed.contains("K")) {
                        exitExists = true;
                        exitRow = rows;
                        
                        if (!isHorizontal && foundPrimaryPiece) {
                            exitCol = pCol;
                        } else {
                            exitCol = currentLine.indexOf('K') - leftPadding;
                            if (exitCol < 0) exitCol = 0;
                        }
                        
                        System.out.println("Found K in line " + i + " (after board rows) at position [" + exitRow + "," + exitCol + "]");
                        break;
                    }
                    
                    int kIndex = currentLine.indexOf('K');
                    if (kIndex != -1) {
                        exitExists = true;
                        exitRow = rows;
                        exitCol = kIndex - leftPadding;
                        if (exitCol < 0) exitCol = 0;
                        
                        System.out.println("Found K character in line after board at col " + exitCol);
                        break;
                    }
                }
            }
            
            if (!exitExists) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.length() > cols + leftPadding) {
                        int endIndex = cols + leftPadding;
                        if (endIndex < currentLine.length() && currentLine.charAt(endIndex) == 'K') {
                            exitExists = true;
                            exitRow = i;
                            exitCol = cols;
                            System.out.println("K found at end of row " + i + " after board width");
                            break;
                        }
                    }
                }
            }
            
            for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                String currentLine = boardLines.get(i);
                if (currentLine.length() <= leftPadding) continue;
                
                for (int j = leftPadding; j < currentLine.length(); j++) {
                    char c = currentLine.charAt(j);
                    
                    if (c == 'P') {
                        if (!foundPrimaryPiece) {
                            pRow = i;
                            pCol = j - leftPadding;
                            foundPrimaryPiece = true;
                        }
                        
                        if (j > leftPadding && currentLine.charAt(j-1) == 'P') isHorizontal = true;
                        if (j < currentLine.length()-1 && currentLine.charAt(j+1) == 'P') isHorizontal = true;
                    }
                }
            }
            
            if (foundPrimaryPiece && !isHorizontal) {
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    if (i == pRow) continue; 
                    
                    String currentLine = boardLines.get(i);
                    int adjustedCol = pCol + leftPadding;
                    
                    if (adjustedCol < currentLine.length() && currentLine.charAt(adjustedCol) == 'P') {
                        isHorizontal = false; 
                        break;
                    }
                }
            }
            
            System.out.println("Primary piece found at [" + pRow + "," + pCol + "], Orientation: " + 
                            (isHorizontal ? "Horizontal" : "Vertical"));
            
            if (exitExists) {
                System.out.println("Exit found at [" + exitRow + "," + exitCol + "]");
                System.out.println("hasLeftK: " + hasLeftK + ", hasTopK: " + hasTopK);
                
                boolean validOrientation = false;
                if (isHorizontal) {
                    if (exitRow == pRow) {
                        validOrientation = true;
                    }
                } else {
                    if (exitCol == pCol) {
                        validOrientation = true;
                    }
                }
                
                if (!validOrientation) {
                    throw new IllegalArgumentException(
                        "Error: Exit door (K) is not aligned with primary piece (P). " +
                        "For horizontal primary pieces, exit must be in the same row. " +
                        "For vertical primary pieces, exit must be in the same column."
                    );
                }
            } else {
                System.out.println("Warning: Exit exists but not detected in parsing stage.");
            }
            
            int finalRows = rows;
            int finalCols = cols;

            if (exitExists && exitRow >= rows) {
                finalRows = exitRow + 1;
                System.out.println("Expanding rows for bottom exit: Final rows = " + finalRows);
            }

            if (exitExists && exitCol >= cols) {
                finalCols = exitCol + 1;
                System.out.println("Expanding cols for right exit: Final cols = " + finalCols);
            }

            if (hasLeftK) {
                finalCols += 1;
                System.out.println("Adding column for left K: Final cols = " + finalCols);
            }

            if (!exitExists && foundPrimaryPiece) {
                if (isHorizontal) {
                    exitRow = pRow;
                    exitCol = cols;
                    finalCols += 1;
                    exitExists = true;
                    System.out.println("Added automatic exit for horizontal at [" + exitRow + "," + exitCol + "]");
                } else {
                    exitRow = rows;
                    exitCol = pCol;
                    finalRows += 1;
                    exitExists = true;
                    System.out.println("Added automatic exit for vertical at [" + exitRow + "," + exitCol + "]");
                }
            }

            currentBoard = new char[finalRows][finalCols];

            if (hasTopK) {
                for (int i = 0; i < finalRows; i++) {
                    for (int j = 0; j < finalCols; j++) {
                        currentBoard[i][j] = '.';
                    }
                }
                
                int adjustedExitCol = exitCol;
                if (hasLeftK) adjustedExitCol += 1; 
                currentBoard[0][adjustedExitCol] = 'K';
                System.out.println("Placed K at top row [0," + adjustedExitCol + "]");
                
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.length() <= leftPadding) continue;
                    
                    for (int j = leftPadding; j < currentLine.length(); j++) {
                        char c = currentLine.charAt(j);
                        if (c == ' ' || c == 'K') continue; 
                        
                        int destCol = j - leftPadding;
                        
                        if (i+1 < finalRows && destCol >= 0 && destCol < finalCols) {
                            currentBoard[i+1][destCol] = c;
                        }
                    }
                }
            } else {
                for (int i = 0; i < finalRows; i++) {
                    for (int j = 0; j < finalCols; j++) {
                        currentBoard[i][j] = '.';
                    }
                }
                
                for (int i = 0; i < Math.min(rows, boardLines.size()); i++) {
                    String currentLine = boardLines.get(i);
                    if (currentLine.length() <= leftPadding) continue;
                    
                    for (int j = leftPadding; j < currentLine.length(); j++) {
                        char c = currentLine.charAt(j);
                        if (c == ' ') continue; 
                        
                        int destCol = j - leftPadding;
                        
                        if (c == 'K' && hasLeftK && j - leftPadding <= 0) {
                            destCol = 0;
                        }
                        
                        if (i >= 0 && i < finalRows && destCol >= 0 && destCol < finalCols) {
                            currentBoard[i][destCol] = c;
                        }
                    }
                }
                
                if (exitExists) {
                    if (hasLeftK) {
                        currentBoard[exitRow][0] = 'K';
                        System.out.println("Placed K at left [" + exitRow + ",0]");
                    }
                    
                    if (exitRow >= rows) {
                        int adjustedExitCol = exitCol;
                        if (hasLeftK) adjustedExitCol += 1;
                        
                        if (!isHorizontal && foundPrimaryPiece) {
                            adjustedExitCol = pCol + (hasLeftK ? 1 : 0);
                        }
                        
                        if (exitRow >= 0 && exitRow < finalRows && adjustedExitCol >= 0 && adjustedExitCol < finalCols) {
                            currentBoard[exitRow][adjustedExitCol] = 'K';
                            System.out.println("Placed K at bottom [" + exitRow + "," + adjustedExitCol + "]");
                        }
                    }
                    
                    if (exitCol >= cols && !hasLeftK) {
                        int adjustedExitCol = finalCols - 1;
                        
                        if (exitRow >= 0 && exitRow < finalRows) {
                            currentBoard[exitRow][adjustedExitCol] = 'K';
                            System.out.println("Placed K at right [" + exitRow + "," + adjustedExitCol + "]");
                        }
                    }
                }
            }

            boolean finalFoundP = false;
            boolean finalFoundK = false;
            boolean finalIsHorizontal = false;
            int finalPRow = -1, finalPCol = -1;
            int finalKRow = -1, finalKCol = -1;
            
            for (int i = 0; i < currentBoard.length; i++) {
                for (int j = 0; j < currentBoard[i].length; j++) {
                    if (currentBoard[i][j] == 'P') {
                        finalFoundP = true;
                        if (finalPRow == -1) {
                            finalPRow = i;
                            finalPCol = j;
                        }
                        
                        if (j > 0 && j < currentBoard[i].length && currentBoard[i][j-1] == 'P') finalIsHorizontal = true;
                        if (j < currentBoard[i].length-1 && currentBoard[i][j+1] == 'P') finalIsHorizontal = true;
                    }
                    if (currentBoard[i][j] == 'K') {
                        finalFoundK = true;
                        finalKRow = i;
                        finalKCol = j;
                    }
                }
            }
            
            if (!finalFoundP) {
                throw new IllegalArgumentException("Error: Primary piece (P) missing from final board.");
            }
            if (!finalFoundK) {
                throw new IllegalArgumentException("Error: Exit door (K) missing from final board.");
            }
            
            boolean finalValidOrientation = false;
            if (finalIsHorizontal) {
                if (finalKRow == finalPRow) finalValidOrientation = true;
            } else {
                if (finalKCol == finalPCol) finalValidOrientation = true;
            }
            
            if (!finalValidOrientation) {
                throw new IllegalArgumentException(
                    "Error: In final board, exit door (K) is not aligned with primary piece (P)."
                );
            }

            System.out.println("Final board contents (" + finalRows + "x" + finalCols + "):");
            for (int i = 0; i < currentBoard.length; i++) {
                System.out.println(new String(currentBoard[i]));
            }
                        
            boardPane.updateBoard(currentBoard);
            
            solutionSteps.clear();
            currentStepIndex = -1;
            
            solutionSteps.add(new MoveStep(copyBoard(currentBoard), null, null, 0));
            
            System.out.println("File berhasil dimuat. Board " + finalRows + "x" + finalCols);
            
        } catch (IOException e) {
            System.err.println("Error loading puzzle file: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            showErrorDialog("Puzzle Error", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error loading puzzle file: " + e.getMessage());
            showErrorDialog("Puzzle Error", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
            }
        }
    }

    public void showErrorDialog(String title, String message) {
        System.err.println("[ERROR DIALOG] " + title + ": " + message);
        
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setTitle(title);
            dialogStage.setResizable(false);
            
            String primaryColor = "#fbb2a2"; 
            String textColor = "#5a3e36";   
            String bgColor = "#ffffff"; 
            
            javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setStyle("-fx-background-color: " + bgColor + "; " +
                        "-fx-border-color: " + primaryColor + "; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px;");
            
            javafx.scene.text.Text titleText = new javafx.scene.text.Text(title);
            titleText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
            titleText.setFill(javafx.scene.paint.Color.web(textColor));
            
            javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(10);
            titleBox.setAlignment(javafx.geometry.Pos.CENTER);
            titleBox.setPadding(new javafx.geometry.Insets(0, 0, 5, 0));
            
            javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(12);
            circle.setFill(javafx.scene.paint.Color.web(primaryColor));
            
            javafx.scene.text.Text icon = new javafx.scene.text.Text("!");
            icon.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
            icon.setFill(javafx.scene.paint.Color.WHITE);
            
            iconPane.getChildren().addAll(circle, icon);
            titleBox.getChildren().addAll(iconPane, titleText);
            
            javafx.scene.shape.Line separator = new javafx.scene.shape.Line();
            separator.setStartX(0);
            separator.setEndX(280);
            separator.setStroke(javafx.scene.paint.Color.web(primaryColor));
            separator.setStrokeWidth(1);
            
            javafx.scene.text.Text messageText = new javafx.scene.text.Text(message);
            messageText.setFont(javafx.scene.text.Font.font("Arial", 14));
            messageText.setFill(javafx.scene.paint.Color.web(textColor));
            messageText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            messageText.setWrappingWidth(280);
            
            javafx.scene.control.Button okButton = new javafx.scene.control.Button("OK");
            okButton.setPrefSize(80, 30);
            okButton.setStyle("-fx-background-color: " + primaryColor + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 4px;");
            
            okButton.setOnMouseEntered(e -> 
                okButton.setStyle("-fx-background-color: #faa18d; " + // Sedikit lebih terang
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 4px;")
            );
            
            okButton.setOnMouseExited(e -> 
                okButton.setStyle("-fx-background-color: " + primaryColor + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 4px;")
            );
            
            okButton.setOnAction(e -> dialogStage.close());
            
            root.getChildren().addAll(titleBox, separator, messageText, okButton);
            
            javafx.scene.Scene dialogScene = new javafx.scene.Scene(root, 320, 180);
            dialogStage.setScene(dialogScene);
            
            root.setEffect(new javafx.scene.effect.DropShadow(5, javafx.scene.paint.Color.rgb(0, 0, 0, 0.2)));
            
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(100), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            dialogStage.setOnShown(e -> fadeIn.play());
            
            dialogStage.showAndWait();
        });
    }

    public void showSuccessDialog(String title, String message) {
        System.out.println("[SUCCESS DIALOG] " + title + ": " + message);
        
        javafx.application.Platform.runLater(() -> {
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setTitle(title);
            dialogStage.setResizable(false);
            
            String primaryColor = "#82BF6E"; 
            String textColor = "#5a3e36";  
            String bgColor = "#ffffff";
            
            javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(15);
            root.setPadding(new javafx.geometry.Insets(20));
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setStyle("-fx-background-color: " + bgColor + "; " +
                        "-fx-border-color: " + primaryColor + "; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 5px;");
            
            javafx.scene.text.Text titleText = new javafx.scene.text.Text(title);
            titleText.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
            titleText.setFill(javafx.scene.paint.Color.web(textColor));
            
            javafx.scene.layout.HBox titleBox = new javafx.scene.layout.HBox(10);
            titleBox.setAlignment(javafx.geometry.Pos.CENTER);
            titleBox.setPadding(new javafx.geometry.Insets(0, 0, 5, 0));
            
            javafx.scene.layout.StackPane iconPane = new javafx.scene.layout.StackPane();
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(12);
            circle.setFill(javafx.scene.paint.Color.web(primaryColor));
            
            javafx.scene.text.Text icon = new javafx.scene.text.Text("✓");
            icon.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
            icon.setFill(javafx.scene.paint.Color.WHITE);
            
            iconPane.getChildren().addAll(circle, icon);
            titleBox.getChildren().addAll(iconPane, titleText);
            
            javafx.scene.shape.Line separator = new javafx.scene.shape.Line();
            separator.setStartX(0);
            separator.setEndX(380);  
            separator.setStroke(javafx.scene.paint.Color.web(primaryColor));
            separator.setStrokeWidth(1);
            
            javafx.scene.text.Text messageText = new javafx.scene.text.Text(message);
            messageText.setFont(javafx.scene.text.Font.font("Arial", 14));
            messageText.setFill(javafx.scene.paint.Color.web(textColor));
            messageText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            messageText.setWrappingWidth(380);
            
            javafx.scene.control.Button okButton = new javafx.scene.control.Button("OK");
            okButton.setPrefSize(80, 30);
            okButton.setStyle("-fx-background-color: " + primaryColor + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 4px;");
            
            okButton.setOnMouseEntered(e -> 
                okButton.setStyle("-fx-background-color: #95CA83; " + 
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 4px;")
            );
            
            okButton.setOnMouseExited(e -> 
                okButton.setStyle("-fx-background-color: " + primaryColor + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 4px;")
            );
            
            okButton.setOnAction(e -> dialogStage.close());
            
            root.getChildren().addAll(titleBox, separator, messageText, okButton);
            
            javafx.scene.Scene dialogScene = new javafx.scene.Scene(root, 420, 200);
            dialogStage.setScene(dialogScene);
            
            root.setEffect(new javafx.scene.effect.DropShadow(5, javafx.scene.paint.Color.rgb(0, 0, 0, 0.2)));
            
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(100), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            dialogStage.setOnShown(e -> fadeIn.play());
            
            dialogStage.showAndWait();
        });
    }

    public boolean solvePuzzle(String algorithm, String heuristic) {
        System.out.println("Start solving puzzle with algorithm: " + algorithm + 
                        ", heuristic: " + (heuristic != null ? heuristic : "N/A"));
        lastUsedAlgorithm = algorithm;
        lastUsedHeuristic = heuristic;
        try {
            debugBoard(currentBoard);
            
            File tempFile = File.createTempFile("rushHourBoard", ".txt");
            tempFile.deleteOnExit();
            
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(currentBoard.length + " " + currentBoard[0].length + "\n");
                
                int numNonPrimaryPieces = countNonPrimaryPieces();
                writer.write(numNonPrimaryPieces + "\n");
                
                for (int i = 0; i < currentBoard.length; i++) {
                    writer.write(new String(currentBoard[i]) + "\n");
                }
                
                writer.write(algorithm + "\n");
                if (!"dijkstra".equals(algorithm.toLowerCase()) && 
                    !"ucs".equals(algorithm.toLowerCase()) && 
                    heuristic != null) {
                    writer.write(heuristic + "\n");
                }
            }
            
            System.out.println("Temporary file created: " + tempFile.getAbsolutePath());
            
            debugReadFile(tempFile);
            
            Board coreBoard = new Board(tempFile.getAbsolutePath());
            System.out.println("Core board loaded");
            
            coreBoard.printBoard(null);
            
            GameState solution = null;
            String algorithmLower = algorithm.toLowerCase().trim();
            
            System.out.println("Running algorithm: " + algorithmLower);
            
            if ("astar".equals(algorithmLower)) {
                System.out.println("Using A* algorithm with " + 
                                (heuristic != null ? heuristic : "manhattan") + " heuristic");
                AStar astar = new AStar(heuristic != null ? heuristic : "manhattan");
                solution = astar.solve(coreBoard);
                nodesVisited = astar.getNodesVisited();
                executionTime = (long) astar.getExecutionTime();

            } else if ("dijkstra".equals(algorithmLower)) {
                System.out.println("Using Dijkstra algorithm");
                Dijkstra dijkstra = new Dijkstra();
                solution = dijkstra.solve(coreBoard);
                nodesVisited = dijkstra.getNodesVisited();
                executionTime = (long) dijkstra.getExecutionTime();

            } else if ("ucs".equals(algorithmLower)) {
                System.out.println("Using UCS algorithm");
                UCS ucs = new UCS(); 
                solution = ucs.solve(coreBoard);
                nodesVisited = ucs.getNodesVisited();
                executionTime = (long) ucs.getExecutionTime();

            } else if ("gbfs".equals(algorithmLower) || "greedy".equals(algorithmLower)) {
                System.out.println("Using GBFS algorithm with " + 
                                (heuristic != null ? heuristic : "manhattan") + " heuristic");
                GBFS gbfs = new GBFS(heuristic != null ? heuristic : "manhattan");
                solution = gbfs.solve(coreBoard);
                nodesVisited = gbfs.getNodesVisited();
                executionTime = (long) gbfs.getExecutionTime();

            } else {
                System.out.println("Unknown algorithm: " + algorithmLower + ". Using A* as fallback");
                AStar astarFallback = new AStar("manhattan");
                solution = astarFallback.solve(coreBoard);
                nodesVisited = astarFallback.getNodesVisited();
                executionTime = (long) astarFallback.getExecutionTime();
            }
            
            if (solution != null) {
                System.out.println("Solution found with " + solution.getMoves().size() + " moves!");
                processAlgorithmSolution(solution);
                return true;
            } else {
                System.out.println("No solution found!");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error solving puzzle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void debugBoard(char[][] board) {
        System.out.println("DEBUG: Board state (" + board.length + "x" + board[0].length + "):");
        for (int i = 0; i < board.length; i++) {
            System.out.println(new String(board[i]));
        }
        
        boolean foundP = false;
        boolean foundK = false;
        int pRow = -1, pCol = -1;
        int kRow = -1, kCol = -1;
        boolean isHorizontal = false;
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 'P') {
                    foundP = true;
                    if (pRow == -1) {
                        pRow = i;
                        pCol = j;
                    }
                    if (j > 0 && j < board[i].length && board[i][j-1] == 'P') isHorizontal = true;
                    if (j < board[i].length-1 && board[i][j+1] == 'P') isHorizontal = true;
                }
                if (board[i][j] == 'K') {
                    foundK = true;
                    kRow = i;
                    kCol = j;
                }
            }
        }
        
        System.out.println("Primary piece (P): " + (foundP ? "Found at [" + pRow + "," + pCol + "]" : "NOT FOUND!"));
        System.out.println("Primary piece orientation: " + (isHorizontal ? "Horizontal" : "Vertical"));
        System.out.println("Exit (K): " + (foundK ? "Found at [" + kRow + "," + kCol + "]" : "NOT FOUND!"));
        
        if (foundP && foundK) {
            boolean validExit = false;
            if (isHorizontal && kRow == pRow) validExit = true;
            if (!isHorizontal && kCol == pCol) validExit = true;
            
            System.out.println("Exit alignment with primary piece: " + (validExit ? "Valid" : "INVALID!"));
        }
    }

    private void debugReadFile(File file) {
        System.out.println("DEBUG: Reading file " + file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println("Line " + (++lineNum) + ": " + line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
        
    private int countNonPrimaryPieces() {
        Set<Character> uniquePieces = new HashSet<>();
        for (int i = 0; i < currentBoard.length; i++) {
            for (int j = 0; j < currentBoard[0].length; j++) {
                char piece = currentBoard[i][j];
                if (piece != '.' && piece != 'K' && piece != 'P') {
                    uniquePieces.add(piece);
                }
            }
        }
        return uniquePieces.size();
    }
    
    private void processAlgorithmSolution(GameState solution) {
        System.out.println("Processing solution with " + solution.getMoves().size() + " moves");
        
        solutionSteps.clear();
        currentStepIndex = -1;
        
        solutionSteps.add(new MoveStep(copyBoard(currentBoard), null, null, 0));
        
        char[][] boardState = copyBoard(currentBoard);
        List<Move> moves = solution.getMoves();
        
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            char piece = move.getPieceId();
            String direction = convertDirectionToGui(move.getDirection());
            int steps = 1; 
            
            try {
                java.lang.reflect.Method getStepsMethod = move.getClass().getMethod("getSteps");
                steps = (int) getStepsMethod.invoke(move);
            } catch (Exception e) {
                steps = 1;
            }
            
            System.out.println("Move " + (i+1) + ": " + piece + " " + direction + " " + steps);
            
            char[][] newBoardState = copyBoard(boardState);
            applyMoveToBoard(newBoardState, piece, direction, steps);
            
            solutionSteps.add(new MoveStep(newBoardState, piece, direction, steps, i + 1));
            
            boardState = newBoardState;
        }
        
        totalMoves = moves.size();
    }
    
    private void applyMoveToBoard(char[][] board, char piece, String direction, int steps) {
        List<int[]> pieceCells = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (board[row][col] == piece) {
                    pieceCells.add(new int[]{row, col});
                }
            }
        }
        
        if (pieceCells.isEmpty()) return;
        
        boolean isHorizontal = true;
        int firstRow = pieceCells.get(0)[0];
        for (int[] cell : pieceCells) {
            if (cell[0] != firstRow) {
                isHorizontal = false;
                break;
            }
        }
        
        int minRow = Integer.MAX_VALUE, minCol = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE, maxCol = Integer.MIN_VALUE;
        
        for (int[] cell : pieceCells) {
            minRow = Math.min(minRow, cell[0]);
            minCol = Math.min(minCol, cell[1]);
            maxRow = Math.max(maxRow, cell[0]);
            maxCol = Math.max(maxCol, cell[1]);
        }
        
        for (int[] cell : pieceCells) {
            board[cell[0]][cell[1]] = '.';
        }
        
        int newMinRow = minRow;
        int newMinCol = minCol;
        
        if (isHorizontal) {
            if (direction.equals("left")) {
                newMinCol = minCol - steps; 
            } else if (direction.equals("right")) {
                newMinCol = minCol + steps; 
            }
        } else { 
            if (direction.equals("up")) {
                newMinRow = minRow - steps; 
            } else if (direction.equals("down")) {
                newMinRow = minRow + steps; 
            }
        }
        
        int pieceHeight = maxRow - minRow + 1;
        int pieceWidth = maxCol - minCol + 1;
        
        for (int i = 0; i < pieceHeight; i++) {
            for (int j = 0; j < pieceWidth; j++) {
                if (newMinRow + i >= 0 && newMinRow + i < board.length && 
                    newMinCol + j >= 0 && newMinCol + j < board[0].length) {
                    board[newMinRow + i][newMinCol + j] = piece;
                }
            }
        }
    }

    private void displayMove(MoveStep step) {
        boardPane.updateBoard(step.board);
        
    }
    
    private String convertDirectionToGui(String algorithmDirection) {
        switch (algorithmDirection.toLowerCase()) {
            case "atas": return "up";
            case "bawah": return "down";
            case "kiri": return "left";
            case "kanan": return "right";
            default: return algorithmDirection;
        }
    }
    
    public void showPreviousMove() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            displayMove(solutionSteps.get(currentStepIndex));
            
        }
    }
    
    public void showNextMove() {
        if (currentStepIndex < solutionSteps.size() - 1) {
            currentStepIndex++;
            displayMove(solutionSteps.get(currentStepIndex));
            
        } else {
            animation.stop();
        }
    }
    
    public void playAnimation() {
        currentStepIndex = 0;
        displayMove(solutionSteps.get(0));
        
        animation.play();
    }
    
    public void stopAnimation() {
        animation.stop();
    }
    
    public void jumpToStep(int stepIndex) {
        if (stepIndex >= 0 && stepIndex < solutionSteps.size()) {
            currentStepIndex = stepIndex;
            displayMove(solutionSteps.get(currentStepIndex));
            
        }
    }
    
    public class MoveStep {
        char[][] board;
        Character piece;
        String direction;
        int steps = 1;
        int moveNumber;
        
        MoveStep(char[][] board, Character piece, String direction, int moveNumber) {
            this(board, piece, direction, 1, moveNumber);
        }
        
        MoveStep(char[][] board, Character piece, String direction, int steps, int moveNumber) {
            this.board = board;
            this.piece = piece;
            this.direction = direction;
            this.steps = steps;
            this.moveNumber = moveNumber;
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
    
    public void saveSolutionToFile(File file) {
        if (solutionSteps.isEmpty() || solutionSteps.size() <= 1) {
            showErrorDialog("Save Error", "No solution available to save.");
            return;
        }

        try (FileWriter writer = new FileWriter(file)) {
            // Write header with fancy formatting
            writer.write("╔══════════════════════════════════════════╗\n");
            writer.write("║        Rush Hour Puzzle Solution         ║\n");
            writer.write("╚══════════════════════════════════════════╝\n\n");
            
            // Write solution information
            writer.write("Solution Details:\n");
            writer.write("----------------\n");
            writer.write(String.format("Algorithm: %s\n", formatAlgorithmName(lastUsedAlgorithm)));
            writer.write(String.format("Heuristic: %s\n", formatHeuristicName(lastUsedHeuristic)));
            writer.write(String.format("Total Moves: %d\n", totalMoves));
            writer.write(String.format("Nodes Visited: %d\n", nodesVisited));
            writer.write(String.format("Execution Time: %d ms\n\n", executionTime));
            
            // Write initial state
            writer.write("Initial State:\n");
            char[][] initialBoard = solutionSteps.get(0).board;
            writeBoardWithBorder(writer, initialBoard);
            writer.write("\n");
            
            // Write each move
            for (int i = 1; i < solutionSteps.size(); i++) {
                MoveStep step = solutionSteps.get(i);
                String directionName = formatDirectionName(step.direction);
                
                writer.write(String.format("Move %d: %s-%s", 
                    step.moveNumber, 
                    step.piece, 
                    directionName));
                
                if (step.steps > 1) {
                    writer.write(String.format(" %d step(s)", step.steps));
                }
                writer.write("\n");
                
                writeBoardWithBorder(writer, step.board);
                writer.write("\n");
            }
            
            // Write footer
            writer.write("End of solution\n");
            writer.write("Generated on: " + java.time.LocalDateTime.now() + "\n");
            
            System.out.println("Solution saved to: " + file.getAbsolutePath());
            
            // Show success dialog using the custom dialog instead of Alert
            javafx.application.Platform.runLater(() -> {
                showSuccessDialog("Success", "Solution successfully saved to:\n" + file.getAbsolutePath());
            });
            
        } catch (IOException e) {
            System.err.println("Error saving solution: " + e.getMessage());
            showErrorDialog("Save Error", "Failed to save solution: " + e.getMessage());
        }
    }

    private void writeBoardWithBorder(FileWriter writer, char[][] board) throws IOException {
        int cols = board[0].length;
        
        // Top border
        writer.write("┌");
        for (int j = 0; j < cols; j++) {
            writer.write("─");
        }
        writer.write("┐\n");
        
        // Board with left and right borders
        for (int i = 0; i < board.length; i++) {
            writer.write("│");
            writer.write(new String(board[i]));
            writer.write("│\n");
        }
        
        // Bottom border
        writer.write("└");
        for (int j = 0; j < cols; j++) {
            writer.write("─");
        }
        writer.write("┘\n");
    }

    /**
     * Formats the algorithm name for display
     */
    private String formatAlgorithmName(String algorithm) {
        if (algorithm == null) return "Unknown";
        
        switch (algorithm.toLowerCase()) {
            case "astar": return "A* (A-Star)";
            case "dijkstra": return "Dijkstra's Algorithm";
            case "gbfs": return "Greedy Best-First Search";
            case "ucs": return "Uniform Cost Search";
            default: return algorithm;
        }
    }

    /**
     * Formats the heuristic name for display
     */
    private String formatHeuristicName(String heuristic) {
        if (heuristic == null) return "None";
        
        switch (heuristic.toLowerCase()) {
            case "manhattan": return "Manhattan Distance";
            case "blocking": return "Blocking Heuristic";
            case "combined": return "Combined Heuristic";
            default: return heuristic;
        }
    }

    /**
     * Formats the direction name for display
     */
    private String formatDirectionName(String direction) {
        if (direction == null) return "Unknown";
        
        switch (direction.toLowerCase()) {
            case "up": return "Up";
            case "down": return "Down";
            case "left": return "Left";
            case "right": return "Right";
            default: return direction;
        }
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