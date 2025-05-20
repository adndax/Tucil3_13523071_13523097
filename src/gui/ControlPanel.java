package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;

public class ControlPanel extends VBox {
    
    private ComboBox<String> algorithmChoiceBox;
    private ComboBox<String> heuristicChoiceBox;
    private Button loadFileButton;
    private Button solveButton;
    private Button nextMoveButton;
    private Button prevMoveButton;
    private Button playAnimationButton;
    private FlowPane stateButtonsPane;  
    private Label statsLabel;
    private Renderer renderer;
    
    public ControlPanel(Renderer renderer) {
        this.renderer = renderer;
        this.setPadding(new Insets(15));
        this.setSpacing(15);
        this.getStyleClass().add("control-panel");
        
        VBox.setVgrow(this, Priority.ALWAYS);
        
        renderer.addStepChangeListener(this::updateStateButtonsFromIndex);
        
        setupControls();
    }
            
    private void setupControls() {
        loadFileButton = new Button("Load Puzzle File");
        loadFileButton.getStyleClass().add("start-button");
        loadFileButton.setId("loadFileButton");
        loadFileButton.setMaxWidth(Double.MAX_VALUE);
        loadFileButton.setOnAction(e -> loadPuzzleFile());
        
        Label algoLabel = new Label("Select Algorithm:");
        algoLabel.getStyleClass().add("control-label");
        
        algorithmChoiceBox = new ComboBox<>();
        // Perbarui daftar algoritma yang tersedia dengan tampilan formal
        algorithmChoiceBox.getItems().addAll("A*", "Dijkstra", "Greedy Best-First Search", "Uniform Cost Search");
        algorithmChoiceBox.setValue("A*");  // Default tampilan formal
        algorithmChoiceBox.setMaxWidth(Double.MAX_VALUE);
        algorithmChoiceBox.getStyleClass().add("control-combo-box");
        
        Label heuristicLabel = new Label("Select Heuristic:");
        heuristicLabel.getStyleClass().add("control-label");
        
        heuristicChoiceBox = new ComboBox<>();
        // Perbarui daftar heuristic yang tersedia dengan tampilan formal
        heuristicChoiceBox.getItems().addAll("None", "Manhattan Distance", "Blocking Heuristic", "Combined Heuristic");
        heuristicChoiceBox.setValue("None");  // Default ke None sesuai permintaan
        heuristicChoiceBox.setMaxWidth(Double.MAX_VALUE);
        heuristicChoiceBox.getStyleClass().add("control-combo-box");

        // Aktifkan/nonaktifkan heuristic berdasarkan algoritma yang dipilih
        algorithmChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean needsHeuristic = !getInternalName(newVal).equals("ucs") && 
                                    !getInternalName(newVal).equals("dijkstra");
            
            if (!needsHeuristic) {
                // Jika algoritma tidak memerlukan heuristik, set ke None dan disable
                heuristicChoiceBox.setValue("None");
                heuristicChoiceBox.setDisable(true);
            } else {
                // Jika algoritma memerlukan heuristik, enable dan set default jika masih None
                heuristicChoiceBox.setDisable(false);
                
                // Jika nilai sebelumnya adalah None dan algoritma memerlukan heuristik,
                // set ke Manhattan Distance secara default
                if (heuristicChoiceBox.getValue().equals("None")) {
                    heuristicChoiceBox.setValue("Manhattan Distance");
                }
            }
        });
        
        // Trigger listener initialization untuk set state awal
        algorithmChoiceBox.fireEvent(
            new javafx.event.ActionEvent(algorithmChoiceBox, null)
        );
        
        solveButton = new Button("Solve Puzzle");
        solveButton.getStyleClass().add("start-button");
        solveButton.setId("solveButton");
        solveButton.setMaxWidth(Double.MAX_VALUE);
        solveButton.setOnAction(e -> solvePuzzle());
        
        prevMoveButton = new Button("← Previous");
        prevMoveButton.getStyleClass().add("nav-button");
        prevMoveButton.setId("prevMoveButton");
        prevMoveButton.setOnAction(e -> renderer.showPreviousMove());
        
        nextMoveButton = new Button("Next →");
        nextMoveButton.getStyleClass().add("nav-button");
        nextMoveButton.setId("nextMoveButton");
        nextMoveButton.setOnAction(e -> renderer.showNextMove());
        
        HBox navigationBox = new HBox(10);
        navigationBox.getChildren().addAll(prevMoveButton, nextMoveButton);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.getStyleClass().add("navigation-box");
        
        playAnimationButton = new Button("▶ Play Animation");
        playAnimationButton.getStyleClass().add("start-button");
        playAnimationButton.setId("playAnimationButton");
        playAnimationButton.setMaxWidth(Double.MAX_VALUE);
        playAnimationButton.setOnAction(e -> renderer.playAnimation());
        
        statsLabel = new Label("Stats: N/A");
        statsLabel.getStyleClass().add("stats-label");
        
        Label statesLabel = new Label("Board States:");
        statesLabel.getStyleClass().add("control-label");
        
        stateButtonsPane = new FlowPane();
        stateButtonsPane.setHgap(5);
        stateButtonsPane.setVgap(5);
        stateButtonsPane.setPrefWrapLength(200); 
        stateButtonsPane.setAlignment(Pos.CENTER_LEFT);
        stateButtonsPane.getStyleClass().add("state-buttons-pane");
        
        ScrollPane stateScrollPane = new ScrollPane(stateButtonsPane);
        stateScrollPane.setFitToWidth(true);
        stateScrollPane.setMaxHeight(200); 
        stateScrollPane.getStyleClass().add("state-scroll-pane");
        stateScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox.setVgrow(stateScrollPane, Priority.ALWAYS);
        
        Separator separator1 = new Separator();
        separator1.getStyleClass().add("control-separator");
        
        Separator separator2 = new Separator();
        separator2.getStyleClass().add("control-separator");
        
        Separator separator3 = new Separator();
        separator3.getStyleClass().add("control-separator");
        
        this.getChildren().addAll(
            loadFileButton,
            algoLabel, algorithmChoiceBox,
            heuristicLabel, heuristicChoiceBox,
            solveButton,
            separator1,
            navigationBox,
            playAnimationButton,
            separator2,
            statsLabel, 
            separator3,
            statesLabel,
            stateScrollPane 
        );
        
        solveButton.setDisable(true);
        prevMoveButton.setDisable(true);
        nextMoveButton.setDisable(true);
        playAnimationButton.setDisable(true);
    }
    
    private void loadPuzzleFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Puzzle File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        File selectedFile = fileChooser.showOpenDialog(this.getScene().getWindow());
        
        if (selectedFile != null) {
            renderer.loadPuzzleFromFile(selectedFile);
            solveButton.setDisable(false);
        }
    }

    private void solvePuzzle() {
        String algorithmFormal = algorithmChoiceBox.getValue();
        String heuristicFormal = heuristicChoiceBox.getValue();
        
        String algorithm = getInternalName(algorithmFormal);
        String heuristic = getInternalName(heuristicFormal);
        
        if ("none".equals(heuristic) || 
            "dijkstra".equals(algorithm) || 
            "ucs".equals(algorithm)) {
            heuristic = null;
        }
        
        System.out.println("Solving puzzle with algorithm: " + algorithm + 
                        ", heuristic: " + (heuristic != null ? heuristic : "N/A"));
        
        solveButton.setDisable(true);
        
        statsLabel.setText("Solving puzzle...");
        try {
            boolean solved = renderer.solvePuzzle(algorithm, heuristic);
            
            if (solved) {
                prevMoveButton.setDisable(false);
                nextMoveButton.setDisable(false);
                playAnimationButton.setDisable(false);
                
                updateStats(
                    renderer.getTotalMoves(),
                    renderer.getNodesVisited(),
                    renderer.getExecutionTime()
                );
                
                createStateButtons();
                
                javafx.application.Platform.runLater(() -> {
                    new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                            javafx.util.Duration.millis(300),
                            event -> renderer.playAnimation()
                        )
                    ).play();
                });
            } else {
                solveButton.setDisable(false);
                
                renderer.showErrorDialog("Solving Result", "No solution found for " + 
                                    algorithmFormal + 
                                    (heuristic != null ? " with " + heuristicFormal : ""));
            }
        } catch (Exception e) {
            solveButton.setDisable(false);
            
            renderer.showErrorDialog("Error", "An error occurred: " + e.getMessage());
            
            System.err.println("Error during puzzle solving: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createStateButtons() {
        stateButtonsPane.getChildren().clear();
        
        int totalStates = renderer.getTotalSteps();
        
        for (int i = 0; i < totalStates; i++) {
            Button stateButton = new Button("S" + i);
            stateButton.getStyleClass().add("state-button");
            stateButton.setPrefSize(35, 25);
            
            if (i == 0) {
                stateButton.getStyleClass().add("active-state-button");
            }
            
            int stateIndex = i;
            stateButton.setOnAction(e -> {
                renderer.jumpToStep(stateIndex);
            });
            
            stateButtonsPane.getChildren().add(stateButton);
        }
    }
    
    private void updateStateButtonsFromIndex(int activeIndex) {
        if (stateButtonsPane.getChildren().isEmpty()) return;
        
        for (int i = 0; i < stateButtonsPane.getChildren().size(); i++) {
            Button btn = (Button) stateButtonsPane.getChildren().get(i);
            if (i == activeIndex) {
                if (!btn.getStyleClass().contains("active-state-button")) {
                    btn.getStyleClass().add("active-state-button");
                }
            } else {
                btn.getStyleClass().remove("active-state-button");
            }
        }
    }
    
    public void updateStats(int moves, int nodes, long executionTime) {
        statsLabel.setText(String.format(
            "Total Moves: %d\nNodes Visited: %d\nExecution Time: %d ms",
            moves, nodes, executionTime
        ));
    }

    private String getInternalName(String displayName) {
        switch(displayName) {
            case "A*": return "astar";
            case "Dijkstra": return "dijkstra";
            case "Greedy Best-First Search": return "gbfs";
            case "Uniform Cost Search": return "ucs";
            case "Manhattan Distance": return "manhattan";
            case "Blocking Heuristic": return "blocking";
            case "Combined Heuristic": return "combined";
            case "None": return "none";
            default: return displayName.toLowerCase();
        }
    }
}