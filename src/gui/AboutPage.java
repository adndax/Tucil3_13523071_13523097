package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AboutPage extends VBox {
    
    private Button backButton;
    
    public AboutPage() {

        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(30));
        this.getStyleClass().add("about-page");
        
        Text title = new Text("What’s Under the Hood?");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.getStyleClass().add("page-title");
        
        TextFlow descriptionFlow = createDescriptionText();
        
        ScrollPane scrollPane = new ScrollPane(descriptionFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.getStyleClass().add("about-scroller");
        
        Label versionLabel = new Label("Version 1.0 - May 2025");
        versionLabel.getStyleClass().add("version-label");
        
        backButton = new Button("Back to Garage");
        backButton.getStyleClass().add("back-button");
        backButton.setPrefWidth(150);
        
        this.getChildren().addAll(
            title,
            scrollPane,
            versionLabel,
            backButton
        );
    }
    
    private TextFlow createDescriptionText() {
        TextFlow textFlow = new TextFlow();
        textFlow.setLineSpacing(5);
        textFlow.setPadding(new Insets(10));
        
        Text intro = new Text(
            "Ever found yourself stuck in a cartoon traffic jam, boxed in by cars that just won’t budge? "
            + "Welcome to Rush Hour, your intelligent parking-lot escape assistant!\n\n"
        );
        intro.setFont(Font.font("Arial", 14));
        
        Text algorithmTitle = new Text("Implemented Algorithms\n");
        algorithmTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Text algorithmDesc = new Text(
            "• A* (A-Star)\n"
            + "A balanced navigator that considers both how far you’ve driven and how close you are to the exit. It’s like having a built-in GPS that actually knows what it’s doing.\n\n"
            + "• Greedy Best First Search (GBFS)\n"
            + "Picks the most promising-looking path — the one that seems closest to the goal. Fast, a little reckless, but great when you’re in a hurry!\n\n"
            + "• Uniform Cost Search (UCS)\n"
            + "The methodical one. It explores every possible route based on the cost to get there. Like a careful driver watching fuel efficiency, it guarantees the optimal solution, even if it takes a few extra turns.\n\n"
        );
        algorithmDesc.setFont(Font.font("Arial", 14));
        
        Text worksTitle = new Text("How it Works\n");
        worksTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text worksInfo = new Text(
            "Just upload a .txt file containing the puzzle configuration, "
            + "our mechanics will read the map, shuffle the cars, and guide the hero straight to the exit.\n\n"
        );
        worksInfo.setFont(Font.font("Arial", 14));
        
        Text licenseTitle = new Text("Licenses\n");
        licenseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text courseInfo = new Text(
            "This project was developed as part of the IF2211 Strategi Algoritma "
            + "course at Institut Teknologi Bandung, Semester II 2024/2025."
        );
        courseInfo.setFont(Font.font("Arial", 14));
        
        textFlow.getChildren().addAll(intro, algorithmTitle, algorithmDesc, worksTitle, worksInfo, licenseTitle, courseInfo);
        return textFlow;
    }
    
    public Button getBackButton() {
        return backButton;
    }
}