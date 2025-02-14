package com.rpg.entities;

import javafx.scene.paint.Color;
import com.rpg.config.GameConfig;
import com.rpg.entities.Weapon;
import javafx.scene.shape.Rectangle;

public class Player extends GameObject {
    
    public Weapon weapon;
    
    public Player(double x, double y, double width, double height, Weapon weapon) {
        super(x, y, width, height, GameConfig.PLAYER_HEALTH);
        this.weapon = weapon;
    }
    
    @Override
    protected void createSprite() {
        sprite = new Rectangle(width, height, Color.BLUE);
        updateSpritePosition();
    }
    
    public Weapon getWeapon() {
    	return this.weapon;
    }
    
    public void setWeapon(Weapon weapon) {
    	this.weapon = weapon;
    }
    // Example move method.
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
        updateSpritePosition();
    }
}

