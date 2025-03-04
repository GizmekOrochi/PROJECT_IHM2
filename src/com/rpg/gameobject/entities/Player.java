package com.rpg.gameobject.entities;

import com.rpg.gameobject.GameObject;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Player extends GameObject {
    public Player(double x, double y, double width, double height, double health) {
        super(x, y, width, height, health);
    }
    
    @Override
    protected void createSprite() {
        sprite = new Rectangle(width, height, Color.BLUE);
        updateSpritePosition();
    }
    
    // Example move method.
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
        updateSpritePosition();
    }
}

