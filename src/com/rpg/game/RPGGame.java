package com.rpg.game;

import com.rpg.config.GameConfig;
import com.rpg.gameobject.GameObjectEnum;
import com.rpg.gameobject.entities.Bullet;
import com.rpg.gameobject.entities.EnemyBullet;
import com.rpg.gameobject.entities.Enemy;
import com.rpg.gameobject.items.Weapon;
import com.rpg.input.PlayerControls;
import com.rpg.map.GameMap;
// IMPORTANT: use TiledMap instead of Map
import com.rpg.map.TiledMap;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RPGGame {
    private static final int WINDOW_WIDTH = GameConfig.WINDOW_WIDTH;
    private static final int WINDOW_HEIGHT = GameConfig.WINDOW_HEIGHT;
    private static final int TILE_SIZE = GameConfig.TILE_SIZE;

    // We'll compute these after we know the map's dimensions
    private double worldWidth;
    private double worldHeight;

    // Player-related
    private static final double PLAYER_SPEED = GameConfig.PLAYER_SPEED;
    private double playerX, playerY;
    private double playerHealth = GameConfig.PLAYER_HEALTH;
    private Rectangle playerRect;

    // Collections for walls, enemies, bullets
    private final List<Rectangle> walls = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();

    private Group worldGroup;
    private AnimationTimer gameLoop;

    // Weapon and control handling
    private Weapon playerWeapon;
    private final PlayerControls playerControls = new PlayerControls();

    // Damage / invincibility
    private static final double ENEMY_DAMAGE = GameConfig.ENEMY_DAMAGE;
    private boolean playerInvincible = false;
    private long invincibleStartTime = 0;
    private static final long INVINCIBLE_DURATION = 1_000_000_000L;

    // The overall game maps manager
    private final GameMap gameMap;
    // The specific TiledMap (level) we are currently in
    private TiledMap currentMap;

    public RPGGame(GameMap gameMap) {
        // Store the passed-in GameMap
        this.gameMap = gameMap;
        // Choose the TiledMap at level 1 by default
        this.currentMap = gameMap.getMap(1);
    }

    public Pane initialize() {
        Pane root = createRootPane();

        // Create a group for the game world
        worldGroup = new Group();
        root.getChildren().add(worldGroup);

        // Generate the tiles for the current map
        generateTiles();
        createPlayer();
        spawnEnemies();

        // IMPORTANT: Add the player rectangle to worldGroup, not root
        if (playerRect != null) {
            worldGroup.getChildren().add(playerRect);
        }

        // Define total world size based on currentMap dimensions
        if (currentMap != null) {
            this.worldWidth = currentMap.getWidth() * TILE_SIZE;
            this.worldHeight = currentMap.getHeight() * TILE_SIZE;
        }

        // Create player's weapon
        playerWeapon = new Weapon(
                GameConfig.PLAYER_SHOOT_INTERVAL,
                GameConfig.PLAYER_BULLET_DAMAGE,
                GameConfig.PLAYER_AMMO_CAPACITY,
                GameConfig.PLAYER_BULLET_SPEED
        );

        // Reload listener
        playerControls.setReloadListener(() -> {
            playerWeapon.reload();
            System.out.println("Weapon reloaded.");
        });

        return root;
    }


    private Pane createRootPane() {
        Pane pane = new Pane();
        pane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        return pane;
    }

    private void generateTiles() {
        if (currentMap == null) {
            System.out.println("No map found at level 1!");
            return;
        }

        int width = currentMap.getWidth();
        int height = currentMap.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GameObjectEnum tileObject = currentMap.getObjectAt(x, y);

                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setX(x * TILE_SIZE);
                tile.setY(y * TILE_SIZE);

                if (tileObject == GameObjectEnum.WALL) {
                    tile.setFill(Color.GRAY);
                    walls.add(tile);
                } else {
                    tile.setFill(Color.LIGHTGREEN);
                }

                worldGroup.getChildren().add(tile);
            }
        }
    }

    private void createPlayer() {
        if (currentMap == null) return;

        int width = currentMap.getWidth();
        int height = currentMap.getHeight();

        // Find the PLAYER object in the TiledMap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (currentMap.getObjectAt(x, y) == GameObjectEnum.PLAYER) {
                    playerX = x * TILE_SIZE;
                    playerY = y * TILE_SIZE;
                    playerRect = new Rectangle(TILE_SIZE, TILE_SIZE, Color.BLUE);
                    playerRect.setX(playerX);
                    playerRect.setY(playerY);
                    return;
                }
            }
        }
    }

    private void spawnEnemies() {
        if (currentMap == null) return;

        int width = currentMap.getWidth();
        int height = currentMap.getHeight();

        // Find ENEMY objects in the TiledMap
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (currentMap.getObjectAt(x, y) == GameObjectEnum.ENEMY) {
                    double enemyX = x * TILE_SIZE;
                    double enemyY = y * TILE_SIZE;
                    Enemy enemy = new Enemy(
                            enemyX, enemyY, worldGroup,
                            GameConfig.ENEMY_HEALTH,
                            GameConfig.ENEMY_SPEED,
                            GameConfig.ENEMY_SHOOT_INTERVAL,
                            GameConfig.ENEMY_BULLET_SPEED,
                            GameConfig.ENEMY_BULLET_DAMAGE,
                            GameConfig.ENEMY_SHOOTING_RANGE
                    );
                    enemies.add(enemy);
                }
            }
        }
    }

    public void addInputHandlers(Scene scene) {
        playerControls.attachInputHandlers(scene);
    }

    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdateTime = 0;

            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                long elapsedNanos = now - lastUpdateTime;
                if (elapsedNanos < GameConfig.OPTIMAL_TIME) {
                    return;
                }
                lastUpdateTime = now;

                update(now);

                // Check if player is dead
                if (playerHealth <= 0) {
                    System.out.println("Game Over!");
                    stop();
                }
            }
        };
        gameLoop.start();
    }

    private void update(long now) {
        updatePlayer();
        updateEnemies();
        updateShooting(now);
        updateBullets();
        updateEnemyShooting(now);
        updateEnemyBullets(now);
        updatePlayerInvincibility(now);
        updateWorldOffset();
        checkPlayerEnemyCollisions(now);
    }

    private void updatePlayer() {
        double dx = 0, dy = 0;
        if (playerControls.isUp())    dy -= PLAYER_SPEED;
        if (playerControls.isDown())  dy += PLAYER_SPEED;
        if (playerControls.isLeft())  dx -= PLAYER_SPEED;
        if (playerControls.isRight()) dx += PLAYER_SPEED;

        double newX = playerX + dx;
        double newY = playerY + dy;

        // Collisions or clamping as needed
        if (!collides(newX, playerY)) {
            playerX = newX;
        }
        if (!collides(playerX, newY)) {
            playerY = newY;
        }

        playerX = clamp(playerX, 0, worldWidth - TILE_SIZE);
        playerY = clamp(playerY, 0, worldHeight - TILE_SIZE);

        // CRITICAL: update the blue rectangle's position to match playerX/Y
        playerRect.setX(playerX);
        playerRect.setY(playerY);
    }

    // Simplified "enemy.update()" call.
    // If your Enemy class needs more data, pass it here.
    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.update(playerX, playerY, currentMap, enemies);
        }
    }

    private void updateShooting(long now) {
        if (playerControls.isShooting()) {
            double worldX = playerControls.getShootX() - worldGroup.getTranslateX();
            double worldY = playerControls.getShootY() - worldGroup.getTranslateY();

            Bullet bullet = playerWeapon.shoot(
                    playerX + TILE_SIZE / 2.0,
                    playerY + TILE_SIZE / 2.0,
                    worldX, worldY,
                    now
            );
            if (bullet != null) {
                bullets.add(bullet);
                worldGroup.getChildren().add(bullet.getSprite());
            }
        }
    }

    private void updateBullets() {
        for (Iterator<Bullet> it = bullets.iterator(); it.hasNext();) {
            Bullet bullet = it.next();
            bullet.update();

            // Out of bounds?
            if (bullet.getX() < 0 || bullet.getX() > worldWidth ||
                bullet.getY() < 0 || bullet.getY() > worldHeight) {
                worldGroup.getChildren().remove(bullet.getSprite());
                it.remove();
                continue;
            }

            // Check wall collisions
            boolean hitWall = false;
            for (Rectangle wall : walls) {
                if (bullet.getSprite().getBoundsInParent().intersects(wall.getBoundsInParent())) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                worldGroup.getChildren().remove(bullet.getSprite());
                it.remove();
                continue;
            }

            // Check enemy collisions
            for (Enemy enemy : enemies) {
                if (enemy.getSprite().isVisible() &&
                    bullet.getSprite().getBoundsInParent().intersects(enemy.getSprite().getBoundsInParent())) {
                    enemy.takeDamage(bullet.getDamage());
                    worldGroup.getChildren().remove(bullet.getSprite());
                    it.remove();
                    break;
                }
            }
        }
    }

    private void updateEnemyShooting(long now) {
        for (Enemy enemy : enemies) {
            EnemyBullet eb = enemy.tryShoot(
                    playerX + TILE_SIZE / 2.0,
                    playerY + TILE_SIZE / 2.0,
                    now
            );
            if (eb != null) {
                enemyBullets.add(eb);
                worldGroup.getChildren().add(eb.getSprite());
            }
        }
    }

    private void updateEnemyBullets(long now) {
        for (Iterator<EnemyBullet> it = enemyBullets.iterator(); it.hasNext();) {
            EnemyBullet eb = it.next();
            eb.update();

            // Out of bounds?
            if (eb.getX() < 0 || eb.getX() > worldWidth ||
                eb.getY() < 0 || eb.getY() > worldHeight) {
                worldGroup.getChildren().remove(eb.getSprite());
                it.remove();
                continue;
            }

            boolean hitWall = false;
            for (Rectangle wall : walls) {
                if (eb.getSprite().getBoundsInParent().intersects(wall.getBoundsInParent())) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                worldGroup.getChildren().remove(eb.getSprite());
                it.remove();
                continue;
            }

            if (eb.getSprite().getBoundsInParent().intersects(playerRect.getBoundsInParent())) {
                if (!playerInvincible) {
                    playerHealth -= eb.getDamage();
                    System.out.println("Player hit by enemy bullet! Health: " + playerHealth);
                    playerInvincible = true;
                    invincibleStartTime = System.nanoTime();
                }
                eb.onHit();
                worldGroup.getChildren().remove(eb.getSprite());
                it.remove();
            }
        }
    }

    private void checkPlayerEnemyCollisions(long now) {
        if (!playerInvincible) {
            for (Enemy enemy : enemies) {
                if (enemy.getSprite().getBoundsInParent().intersects(playerRect.getBoundsInParent())) {
                    playerHealth -= ENEMY_DAMAGE;
                    playerInvincible = true;
                    invincibleStartTime = now;
                    System.out.println("Player hit by enemy! Health: " + playerHealth);
                    break;
                }
            }
        }
    }

    private void updatePlayerInvincibility(long now) {
        if (playerInvincible && now - invincibleStartTime >= INVINCIBLE_DURATION) {
            playerInvincible = false;
            System.out.println("Player is no longer invincible.");
        }
    }

    private boolean collides(double x, double y) {
        Rectangle temp = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
        for (Rectangle wall : walls) {
            if (temp.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateWorldOffset() {
        double offsetX = WINDOW_WIDTH / 2.0 - (playerX + TILE_SIZE / 2.0);
        double offsetY = WINDOW_HEIGHT / 2.0 - (playerY + TILE_SIZE / 2.0);
        worldGroup.setTranslateX(offsetX);
        worldGroup.setTranslateY(offsetY);
    }

    public double getPlayerHealth() {
        return playerHealth;
    }
}

