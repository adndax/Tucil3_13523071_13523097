package gui;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class WelcomePage extends VBox {
    
    private Button startButton;
    private Button creatorButton;
    private Button aboutButton;
    
    public WelcomePage() {
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(30));
        this.getStyleClass().add("welcome-page");
        
        ImageView logoView = createLogoImageView();
        
        startButton = createStyledButton("Start the Engine", "start-button");
        creatorButton = createStyledButton("Creators", "nav-button");
        aboutButton = createStyledButton("About", "nav-button");
        
        this.getChildren().addAll(
            logoView,
            startButton,
            creatorButton,
            aboutButton
        );
    }
    
    private ImageView createLogoImageView() {
        try {
            File imageFile = new File("resources/images/car.png");
            if (!imageFile.exists()) {
                System.err.println("File tidak ditemukan: " + imageFile.getAbsolutePath());
                throw new Exception("Logo file not found");
            }
            
            Image logoImage = new Image(imageFile.toURI().toString());
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitWidth(280);
            logoView.setPreserveRatio(true);
            return logoView;
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
    
    private Button createStyledButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        return button;
    }
    
    // Getters
    public Button getStartButton() {
        return startButton;
    }
    
    public Button getCreatorButton() {
        return creatorButton;
    }
    
    public Button getAboutButton() {
        return aboutButton;
    }
    
}