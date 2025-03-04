package com.rpg.gameobject;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public abstract class GameObject {
    protected double x, y;
    protected double health;
    protected double width, height;
    protected Node sprite;

    public GameObject(double x, double y, double width, double height, double health) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = health;
        createSprite();
    }
    
    // Each subclass creates its own visual representation.
    protected abstract void createSprite();

    public Node getSprite() {
        return sprite;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }

    // Common method to apply damage.
    public void takeDamage(double damage) {
        health -= damage;
        if (health < 0) {
            health = 0;
        }
    }

    /**
     * Updates the sprite's position according to the object's x and y.
     * If the sprite is a Rectangle, it sets its x and y.
     * If it's a Circle, it sets its center (offset by its radius).
     */
    public void updateSpritePosition() {
        if (sprite instanceof Rectangle) {
            ((Rectangle) sprite).setX(x);
            ((Rectangle) sprite).setY(y);
        } else if (sprite instanceof Circle) {
            double radius = ((Circle) sprite).getRadius();
            ((Circle) sprite).setCenterX(x + radius);
            ((Circle) sprite).setCenterY(y + radius);
        }
    }
}

