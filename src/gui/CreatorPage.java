package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CreatorPage extends VBox {
    
    private Button backButton;
    
    public CreatorPage() {

        this.setSpacing(30);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(30));
        this.getStyleClass().add("creator-page");
        
        Text title = new Text("Meet the Mechanics");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.getStyleClass().add("page-title");

        Text description = new Text("Under the hood of this solver are two engineers fine-tuning every move. \nThey don’t fix real engines, they fix logic puzzles!");
        description.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        description.getStyleClass().add("description-text");

        HBox creatorLayout = createCreatorLayout();

        backButton = new Button("Back to Garage");
        backButton.getStyleClass().add("back-button");
        backButton.setPrefWidth(150);
        
        this.getChildren().addAll(
            title,
            description,
            creatorLayout,
            backButton
        );
    }

    private HBox createCreatorLayout() {
        HBox layout = new HBox(30);
        layout.setAlignment(Pos.CENTER);
        
        VBox leftCreator = new VBox(10);
        leftCreator.setAlignment(Pos.CENTER_RIGHT);
        
        Label nameLeft = new Label("Shanice Feodora");
        nameLeft.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLeft.getStyleClass().add("creator-name");
        
        Label nimLeft = new Label("13523097");
        nimLeft.getStyleClass().add("creator-details");
        
        leftCreator.getChildren().addAll(nameLeft, nimLeft);
        
        ImageView creatorImage = createCreatorImageView();
        
        VBox rightCreator = new VBox(10);
        rightCreator.setAlignment(Pos.CENTER_LEFT);
        
        Label nameRight = new Label("Adinda Putri");
        nameRight.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameRight.getStyleClass().add("creator-name");
        
        Label nimRight = new Label("13523071");
        nimRight.getStyleClass().add("creator-details");
        
        rightCreator.getChildren().addAll(nameRight, nimRight);
        
        layout.getChildren().addAll(leftCreator, creatorImage, rightCreator);
        
        return layout;
    }

    private ImageView createCreatorImageView() {
        try {
            Image creatorImage = new Image(getClass().getResourceAsStream("/resources/images/creator.png"));
            ImageView creatorView = new ImageView(creatorImage);
            creatorView.setFitWidth(450);
            creatorView.setPreserveRatio(true);
            return creatorView;
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
    
    public Button getBackButton() {
        return backButton;
    }
}