package com.rpg.game;

import com.rpg.game.RPGGame;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import com.rpg.map.GameMap;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
    	GameMap map = new GameMap();
        RPGGame game = new RPGGame(map);
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

