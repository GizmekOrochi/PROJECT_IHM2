package com.rpg.game;

import com.rpg.game.RPGGame;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        RPGGame game = new RPGGame();
        Pane root = game.initialize();
        Scene scene = new Scene(root);
        game.addInputHandlers(scene);

        primaryStage.setTitle("2D RPG Game with Moving World");
        primaryStage.setScene(scene);
        primaryStage.show();

        game.startGameLoop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

