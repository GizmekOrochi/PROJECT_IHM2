package com.rpg.launcher;

import com.rpg.config.GameConfig;
import com.rpg.game.RPGGame;
import com.rpg.input.PlayerControls;
import com.rpg.map.GameMap;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GameLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create or load your game map
        GameMap gameMap = new GameMap();

        // Initialize game logic
        RPGGame game = new RPGGame(gameMap);
        Pane root = game.initialize();
        Scene scene = new Scene(root);

        // Create PlayerControls instance and attach its handlers
        PlayerControls playerControls = new PlayerControls();
        playerControls.attachInputHandlers(scene);

        // Set reload listener: right-click triggers reload
        playerControls.setReloadListener(() -> game.reloadWeapon());

        // AnimationTimer polls PlayerControls state and calls RPGGame methods.
        new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                // Only update if enough time has elapsed based on our slowdown config.
                long elapsed = now - lastUpdate;
                if (elapsed < GameConfig.OPTIMAL_TIME) {
                    return;
                }
                
                // Poll movement and dodge inputs
                game.onMoveUp(playerControls.isUp());
                game.onMoveDown(playerControls.isDown());
                game.onMoveLeft(playerControls.isLeft());
                game.onMoveRight(playerControls.isRight());
                if (playerControls.isDodgeRequested()) {
                    game.onDodgeRequest();
                    // Optionally consume the dodge request if desired:
                    // playerControls.consumeDodgeRequest();
                }
                
                // Check if shooting is active and call onShoot with current mouse coordinates.
                if (playerControls.isShooting()) {
                    game.onShoot(playerControls.getShootX(), playerControls.getShootY(), now);
                }
                
                // Update game logic
                game.update(now);
                lastUpdate = now;
            }
        }.start();

        primaryStage.setTitle("2D RPG Game with Moving World");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

