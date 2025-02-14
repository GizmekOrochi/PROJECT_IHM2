package com.rpg.entities;

import com.rpg.config.GameConfig;
import com.rpg.util.BFSPathfinding;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.List;

public class Enemy extends GameObject {
    private static final double ENEMY_SIZE = GameConfig.ENEMY_SIZE;
    private static final int TILE_SIZE = GameConfig.TILE_SIZE;
    private static final int RANGE = GameConfig.ENEMY_RANGE;
    
    private double enemySpeed;
    private boolean alive;
    private Group worldGroup;
    
    // Shooting fields.
    private long lastShotTimeEnemy = 0;
    private long enemyShootInterval;
    private double enemyBulletSpeed;
    private double enemyBulletDamage;
    private double shootingRange;
    
    public Enemy(double x, double y, Group worldGroup,
                 double health, double enemySpeed, long enemyShootInterval,
                 double enemyBulletSpeed, double enemyBulletDamage, double shootingRange) {
        super(x, y, ENEMY_SIZE, ENEMY_SIZE, health);
        this.worldGroup = worldGroup;
        this.enemySpeed = enemySpeed;
        this.enemyShootInterval = enemyShootInterval;
        this.enemyBulletSpeed = enemyBulletSpeed;
        this.enemyBulletDamage = enemyBulletDamage;
        this.shootingRange = shootingRange;
        this.alive = true;
      
        if (worldGroup != null && sprite != null) {
            worldGroup.getChildren().add(sprite);
        }
    }
    
    @Override
    protected void createSprite() {
        sprite = new Rectangle(width, height, Color.RED);
        updateSpritePosition();
    }
    
    public void update(double playerX, double playerY, int[][] envMatrix, List<Rectangle> enemyRectangles) {
        if (!alive) return;
        
        List<int[]> path = BFSPathfinding.findPath(envMatrix, (int)(x / TILE_SIZE), (int)(y / TILE_SIZE),
                                                   (int)(playerX / TILE_SIZE), (int)(playerY / TILE_SIZE));
        if (Math.hypot(playerX - x, playerY - y) > RANGE && path.size() > 1) {
            int[] nextTile = path.get(1);
            double targetX = nextTile[0] * TILE_SIZE + TILE_SIZE / 2.0 - width / 2.0;
            double targetY = nextTile[1] * TILE_SIZE + TILE_SIZE / 2.0 - height / 2.0;
            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.hypot(dx, dy);
            if (distance > 0) {
                double step = Math.min(enemySpeed, distance);
                double newX = x + step * dx / distance;
                double newY = y + step * dy / distance;
                if (!collidesWithEnemies(newX, newY, enemyRectangles)) {
                    x = newX;
                    y = newY;
                    updateSpritePosition();
                }
            }
        }
    }
    
    private boolean collidesWithEnemies(double newX, double newY, List<Rectangle> enemyRectangles) {
        Rectangle tempRect = new Rectangle(newX, newY, width, height);
        for (Rectangle other : enemyRectangles) {
            if (other == sprite) continue;
            if (tempRect.getBoundsInParent().intersects(other.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }
    
    public EnemyBullet tryShoot(double playerCenterX, double playerCenterY, long now) {
        if (!alive) return null;
        double enemyCenterX = x + width / 2.0;
        double enemyCenterY = y + height / 2.0;
        double distance = Math.hypot(playerCenterX - enemyCenterX, playerCenterY - enemyCenterY);
        if (distance > shootingRange) {
            return null;
        }
        if (now - lastShotTimeEnemy >= enemyShootInterval) {
            lastShotTimeEnemy = now;
            return new EnemyBullet(enemyCenterX, enemyCenterY, playerCenterX, playerCenterY,
                                   enemyBulletSpeed, enemyBulletDamage);
        }
        return null;
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    @Override
    public void takeDamage(double damage) {
        super.takeDamage(damage);
        if (health <= 0) {
            alive = false;
            sprite.setVisible(false);
        }
    }
}

