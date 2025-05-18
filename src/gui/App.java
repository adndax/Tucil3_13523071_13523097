package gui;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class App extends Application {
    
    private enum PageType {
        WELCOME, GAME, CREATOR, ABOUT
    }
    
    private StackPane rootContainer;
    
    private BoardPane boardPane;
    private ControlPanel controlPanel;
    private Renderer renderer;
    private BorderPane gameRoot;
    
    private Map<PageType, Node> pages = new HashMap<>();
    private PageType currentPage = null;
    
    @Override
    public void start(Stage primaryStage) {
        rootContainer = new StackPane();
        
        setupGameComponents();
        
        setupPages();
        
        navigateTo(PageType.WELCOME);
        
        Scene scene = new Scene(rootContainer, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        primaryStage.setTitle("Rush Hour");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    private void setupGameComponents() {
        boardPane = new BoardPane();
        renderer = new Renderer(boardPane);
        controlPanel = new ControlPanel(renderer);
        
        BorderPane boardContainer = new BorderPane();
        boardContainer.setCenter(boardPane);
        boardContainer.setPrefWidth(Double.MAX_VALUE);
        boardContainer.setPrefHeight(Double.MAX_VALUE);
        
        boardPane.setStyle("-fx-fit-to-width: true;");
        
        gameRoot = new BorderPane();
        
        StackPane controlContainer = new StackPane();
        controlContainer.getChildren().add(controlPanel);
        
        controlContainer.setPadding(new Insets(0, 15, 11, 0)); 
        
        controlPanel.setMinHeight(controlPanel.getPrefHeight());
        controlPanel.setMaxHeight(controlPanel.getPrefHeight());
        controlPanel.setMinWidth(controlPanel.getPrefWidth());
        controlPanel.setMaxWidth(controlPanel.getPrefWidth());
        
        controlPanel.setStyle("-fx-fit-to-height: false; -fx-fit-to-width: false;");
        
        BorderPane centerContent = new BorderPane();
        centerContent.setCenter(boardContainer);
        centerContent.setRight(controlContainer);
        
        gameRoot.setCenter(centerContent);
        gameRoot.getStyleClass().add("game-root");
        
        setupMenuBar();
    }

    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        menuBar.setStyle(null);
        menuBar.getStyleClass().clear();
        menuBar.getStyleClass().add("menu-bar");
        
        Menu fileMenu = new Menu("File");
        fileMenu.getStyleClass().add("custom-menu");
        
        MenuItem welcomeItem = new MenuItem("Main Menu");
        welcomeItem.getStyleClass().add("custom-menu-item");
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.getStyleClass().add("custom-menu-item");
        
        welcomeItem.setOnAction(e -> navigateTo(PageType.WELCOME));
        exitItem.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(welcomeItem, new SeparatorMenuItem(), exitItem);
        
        Menu viewMenu = new Menu("View");
        viewMenu.getStyleClass().add("custom-menu");
        
        MenuItem creatorItem = new MenuItem("Creators");
        creatorItem.getStyleClass().add("custom-menu-item");
        
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.getStyleClass().add("custom-menu-item");
        
        creatorItem.setOnAction(e -> navigateTo(PageType.CREATOR));
        aboutItem.setOnAction(e -> navigateTo(PageType.ABOUT));
        
        viewMenu.getItems().addAll(creatorItem, aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu);
        
        BorderPane menuContainer = new BorderPane();
        menuContainer.setTop(menuBar);
        menuContainer.setStyle("-fx-background-color: #725861;");
        
        gameRoot.setTop(menuContainer);
        
        BorderPane.setMargin(menuContainer, new Insets(0, 0, 15, 0));
    }
        
    private void setupPages() {
        WelcomePage welcomePage = new WelcomePage();
        welcomePage.getStartButton().setOnAction(e -> navigateTo(PageType.GAME));
        welcomePage.getCreatorButton().setOnAction(e -> navigateTo(PageType.CREATOR));
        welcomePage.getAboutButton().setOnAction(e -> navigateTo(PageType.ABOUT));
        
        CreatorPage creatorPage = new CreatorPage();
        creatorPage.getBackButton().setOnAction(e -> navigateTo(PageType.WELCOME));
        
        AboutPage aboutPage = new AboutPage();
        aboutPage.getBackButton().setOnAction(e -> navigateTo(PageType.WELCOME));
        
        pages.put(PageType.WELCOME, welcomePage);
        pages.put(PageType.GAME, gameRoot);
        pages.put(PageType.CREATOR, creatorPage);
        pages.put(PageType.ABOUT, aboutPage);
    }
    
    private void navigateTo(PageType pageType) {
        if (currentPage == pageType) return;
        
        Node oldContent = currentPage != null ? pages.get(currentPage) : null;
        Node newContent = pages.get(pageType);
        
        if (oldContent != null) {
            switchContent(oldContent, newContent);
        } else {
            rootContainer.getChildren().clear();
            newContent.setOpacity(1.0);
            rootContainer.getChildren().add(newContent);
        }
        
        currentPage = pageType;
    }
    
    private void switchContent(Node oldContent, Node newContent) {
        Duration duration = Duration.millis(300);
        
        FadeTransition fadeOut = new FadeTransition(duration, oldContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        newContent.setOpacity(0.0);
        
        if (!rootContainer.getChildren().contains(newContent)) {
            rootContainer.getChildren().add(newContent);
        }
        
        FadeTransition fadeIn = new FadeTransition(duration, newContent);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        
        fadeOut.setOnFinished(e -> {
            rootContainer.getChildren().remove(oldContent);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}