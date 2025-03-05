package com.rpg.gameobject.entities;

import com.rpg.config.GameConfig;
import com.rpg.gameobject.GameObject;
import com.rpg.gameobject.GameObjectEnum;
import com.rpg.map.TiledMap;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

import static com.rpg.config.GameConfig.TILE_SIZE;

public class Enemy extends GameObject {
    private static final double ENEMY_SIZE = GameConfig.ENEMY_SIZE;
    private static final int RANGE = GameConfig.ENEMY_RANGE;

    private double enemySpeed;
    private boolean alive;
    private Group worldGroup;

    // Shooting fields
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

        // Create the sprite (a red rectangle)
        if (worldGroup != null) {
            createSprite();
            worldGroup.getChildren().add(sprite);
        }
    }

    @Override
    protected void createSprite() {
        sprite = new Rectangle(width, height, Color.RED);
        updateSpritePosition();
    }

    /**
     * Enemy chases the player if they're outside the 'RANGE'.
     * Collisions are checked against walls (using TiledMap) and other enemies (using enemyRectangles).
     */
    public void update(double playerX, double playerY, TiledMap map, List<Enemy> enemies) {
        if (!alive) return;

        double dx = playerX - x;
        double dy = playerY - y;
        double distance = Math.hypot(dx, dy);

        // If the player is outside the "RANGE", the enemy attempts to move closer
        if (distance > RANGE) {
            double step = Math.min(enemySpeed, distance); 
            // Move proportionally in the direction of the player
            double stepX = (dx / distance) * step;
            double stepY = (dy / distance) * step;

            double newX = x + stepX;
            double newY = y + stepY;

            // Only move if there's no wall or enemy collision
            if (!collidesWithWalls(newX, newY, map) &&
                !collidesWithEnemies(newX, newY, enemies)) {
                x = newX;
                y = newY;
                updateSpritePosition();
            }
        }
    }

    /**
     * Checks if the new position (newX, newY) is blocked by a wall tile in the TiledMap.
     */
    private boolean collidesWithWalls(double newX, double newY, TiledMap map) {
        // Determine the range of tiles that the enemy covers.
        int leftTile = (int)(newX / TILE_SIZE);
        int rightTile = (int)((newX + width - 1) / TILE_SIZE);
        int topTile = (int)(newY / TILE_SIZE);
        int bottomTile = (int)((newY + height - 1) / TILE_SIZE);

        // Check each tile in the enemy's bounding box.
        for (int x = leftTile; x <= rightTile; x++) {
            for (int y = topTile; y <= bottomTile; y++) {
                // Out of bounds implies collision (or can be treated as blocked).
                if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight()) {
                    return true;
                }
                // If any tile within the bounding box is a wall, report a collision.
                if (map.getObjectAt(x, y) == GameObjectEnum.WALL) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks if moving to (newX, newY) would overlap another enemy's Rectangle.
     */
    private boolean collidesWithEnemies(double newX, double newY, List<Enemy> enemies) {
        Rectangle tempRect = new Rectangle(newX, newY, width, height);

        for (Enemy otherEnemy : enemies) {
        // Skip this same enemy
            if (otherEnemy == this) continue;
    
            Rectangle otherSprite = (Rectangle) otherEnemy.getSprite();
            if (otherSprite != null &&
                tempRect.getBoundsInParent().intersects(otherSprite.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempt to shoot at the player if within shootingRange and enough time has passed.
     */
    public EnemyBullet tryShoot(double playerCenterX, double playerCenterY, long now) {
        if (!alive) return null;

        double enemyCenterX = x + width / 2.0;
        double enemyCenterY = y + height / 2.0;
        double distance = Math.hypot(playerCenterX - enemyCenterX, playerCenterY - enemyCenterY);

        // Only shoot if within shootingRange
        if (distance > shootingRange) {
            return null;
        }

        // Rate-of-fire check
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
            if (sprite != null) {
                sprite.setVisible(false);
            }
        }
    }
}

