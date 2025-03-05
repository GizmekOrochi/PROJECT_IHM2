package com.rpg.view;

import com.rpg.config.GameConfig;
import com.rpg.model.GameModel;
import com.rpg.controler.GameControler;
import com.rpg.map.GameMap;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GameView extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameMap gameMap = new GameMap();

        GameModel model = new GameModel(gameMap);
        Pane root = model.initialize();
        Scene scene = new Scene(root);

        GameControler controler = new GameControler();
        controler.attachInputHandlers(scene);
        controler.setReloadListener(() -> model.reloadWeapon());

        new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                long elapsed = now - lastUpdate;
                if (elapsed < GameConfig.OPTIMAL_TIME) {
                    return;
                }
                
                model.onMoveUp(controler.isUp());
                model.onMoveDown(controler.isDown());
                model.onMoveLeft(controler.isLeft());
                model.onMoveRight(controler.isRight());
                if (controler.isDodgeRequested()) {
                    model.onDodgeRequest();
                }

                if (controler.isShooting()) {
                    model.onShoot(controler.getShootX(), controler.getShootY(), now);
                }
                
                // Update game logic
                model.update(now);
                lastUpdate = now;
            }
        }.start();

        primaryStage.setTitle("2D RPG Game with Moving World");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

