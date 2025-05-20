package gui;

import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SaveSolutionHandler {

    private final Renderer renderer;
    private final Button saveSolutionButton;
    private boolean puzzleSolved = false;
    private boolean fileLoaded = false;

    public SaveSolutionHandler(Renderer renderer) {
        this.renderer = renderer;
        this.saveSolutionButton = createSaveSolutionButton();
        updateButtonState();
    }

    private Button createSaveSolutionButton() {
        Button button = new Button("Save Solution");
        button.getStyleClass().add("start-button"); 
        button.setId("saveSolutionButton");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> handleSaveSolution());
        button.setDisable(true); 
        return button;
    }

    public Button getSaveSolutionButton() {
        return saveSolutionButton;
    }

    private void handleSaveSolution() {
        if (!puzzleSolved || renderer.getTotalSteps() <= 1) {
            renderer.showErrorDialog("Save Error", "No solution available to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Solution");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String initialFileName = "rushhour_solution_" + dateStr + ".txt";
        fileChooser.setInitialFileName(initialFileName);
        
        File file = fileChooser.showSaveDialog(saveSolutionButton.getScene().getWindow());
        
        if (file != null) {
            renderer.saveSolutionToFile(file);
        }
    }

    public void setFileLoaded(boolean loaded) {
        this.fileLoaded = loaded;
        if (!loaded) {
            this.puzzleSolved = false;
        }
        updateButtonState();
    }
    
    public void setPuzzleSolved(boolean solved) {
        this.puzzleSolved = solved;
        updateButtonState();
    }
 
    private void updateButtonState() {
        boolean shouldEnable = fileLoaded && puzzleSolved;
        saveSolutionButton.setDisable(!shouldEnable);
    }
}