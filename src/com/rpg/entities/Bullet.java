package com.rpg.entities;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet extends GameObject {
    private double dx, dy;
    private double speed;
    private double damage;

    public Bullet(double x, double y, double targetX, double targetY, double speed, double damage) {
        super(x, y, 10, 10, 1);
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double length = Math.hypot(deltaX, deltaY);
        if (length == 0) {
            this.dx = 1;
            this.dy = 0;
        } else {
            this.dx = deltaX / length;
            this.dy = deltaY / length;
        }
        this.speed = speed;
        this.damage = damage;
    }
    
    @Override
    protected void createSprite() {
        Circle circle = new Circle(5, Color.YELLOW);
        circle.setCenterX(x + 5);
        circle.setCenterY(y + 5);
        this.sprite = circle;
    }
    
    public void update() {
        x += dx * speed;
        y += dy * speed;
        ((Circle) sprite).setCenterX(x + 5);
        ((Circle) sprite).setCenterY(y + 5);
    }

    public double getDamage() {
        return damage;
    }
}

