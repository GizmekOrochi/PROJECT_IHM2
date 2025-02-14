package com.rpg.game;

import com.rpg.config.GameConfig;
import com.rpg.config.EnvironmentMatrix;
import com.rpg.config.EnemyMatrix;
import com.rpg.entities.Bullet;
import com.rpg.entities.EnemyBullet;
import com.rpg.entities.Weapon;
import com.rpg.entities.Enemy;
import com.rpg.input.PlayerControls;
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
    private static final int WORLD_WIDTH = GameConfig.WORLD_WIDTH;
    private static final int WORLD_HEIGHT = GameConfig.WORLD_HEIGHT;
    private static final double PLAYER_SPEED = GameConfig.PLAYER_SPEED;
    
    private double playerX = 50, playerY = 50;
    private double playerHealth = GameConfig.PLAYER_HEALTH;
    private Rectangle playerRect;
    private final List<Rectangle> walls = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private Group worldGroup;
    
    private final EnvironmentMatrix environmentMatrix = new EnvironmentMatrix();
    private final EnemyMatrix enemyMatrix = new EnemyMatrix();
    private final PlayerControls playerControls = new PlayerControls();
    
    private Weapon playerWeapon;
    
    private static final double enemy_damage = GameConfig.ENEMY_DAMAGE;
    
    private boolean playerInvincible = false;
    private long invincibleStartTime = 0;
    private static final long INVINCIBLE_DURATION = 1_000_000_000L;
    
    private AnimationTimer gameLoop;
    
    public Pane initialize() {
        Pane root = createRootPane();
        worldGroup = new Group();
        generateTiles();
        root.getChildren().add(worldGroup);
        createPlayer();
        root.getChildren().add(playerRect);
        spawnEnemies();
        
        playerWeapon = new Weapon(
                GameConfig.PLAYER_SHOOT_INTERVAL,
                GameConfig.PLAYER_BULLET_DAMAGE,
                GameConfig.PLAYER_AMMO_CAPACITY,
                GameConfig.PLAYER_BULLET_SPEED
        );
        
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
        int[][] matrix = environmentMatrix.getMatrix();
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setX(col * TILE_SIZE);
                tile.setY(row * TILE_SIZE);
                if (matrix[row][col] == 1) {
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
        playerRect = new Rectangle(TILE_SIZE, TILE_SIZE, Color.BLUE);
        playerRect.setX(WINDOW_WIDTH / 2 - TILE_SIZE / 2);
        playerRect.setY(WINDOW_HEIGHT / 2 - TILE_SIZE / 2);
    }
    
    private void spawnEnemies() {
        int[][] enemyPositions = enemyMatrix.getMatrix();
        for (int row = 0; row < enemyPositions.length; row++) {
            for (int col = 0; col < enemyPositions[row].length; col++) {
                if (enemyPositions[row][col] == 1) {
                    double enemyX = col * TILE_SIZE;
                    double enemyY = row * TILE_SIZE;
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
            @Override
            public void handle(long now) {
                update(now);
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
        
        if (!collides(newX, playerY)) {
            playerX = newX;
        }
        if (!collides(playerX, newY)) {
            playerY = newY;
        }
        
        playerX = clamp(playerX, 0, WORLD_WIDTH - TILE_SIZE);
        playerY = clamp(playerY, 0, WORLD_HEIGHT - TILE_SIZE);
    }
    
    private void updateEnemies() {
        int[][] matrix = environmentMatrix.getMatrix();
        List<Rectangle> enemyRectangles = new ArrayList<>();
        for (Enemy enemy : enemies) {
            enemyRectangles.add((Rectangle) enemy.getSprite());
        }
        for (Enemy enemy : enemies) {
            enemy.update(playerX, playerY, matrix, enemyRectangles);
        }
    }
    
    private void updateShooting(long now) {
        if (playerControls.isShooting()) {
            double worldX = playerControls.getShootX() - worldGroup.getTranslateX();
            double worldY = playerControls.getShootY() - worldGroup.getTranslateY();
            Bullet bullet = playerWeapon.shoot(
                    playerX + TILE_SIZE / 2.0,
                    playerY + TILE_SIZE / 2.0,
                    worldX, worldY, now
            );
            if (bullet != null) {
                bullets.add(bullet);
                worldGroup.getChildren().add(bullet.getSprite());
            }
        }
    }
    
    private void updateBullets() {
        for (Iterator<Bullet> iterator = bullets.iterator(); iterator.hasNext(); ) {
            Bullet bullet = iterator.next();
            bullet.update();
            if (bullet.getX() < 0 || bullet.getX() > WORLD_WIDTH ||
                bullet.getY() < 0 || bullet.getY() > WORLD_HEIGHT) {
                worldGroup.getChildren().remove(bullet.getSprite());
                iterator.remove();
                continue;
            }
            boolean hitWall = false;
            for (Rectangle wall : walls) {
                if (bullet.getSprite().getBoundsInParent().intersects(wall.getBoundsInParent())) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                worldGroup.getChildren().remove(bullet.getSprite());
                iterator.remove();
                continue;
            }
            for (Enemy enemy : enemies) {
                if (enemy.getSprite().isVisible() &&
                    bullet.getSprite().getBoundsInParent().intersects(enemy.getSprite().getBoundsInParent())) {
                    enemy.takeDamage(bullet.getDamage());
                    worldGroup.getChildren().remove(bullet.getSprite());
                    iterator.remove();
                    break;
                }
            }
        }
    }
    
    private void updateEnemyShooting(long now) {
        for (Enemy enemy : enemies) {
            EnemyBullet eb = enemy.tryShoot(
                    playerX + TILE_SIZE / 2.0,
                    playerY + TILE_SIZE / 2.0, now
            );
            if (eb != null) {
                enemyBullets.add(eb);
                worldGroup.getChildren().add(eb.getSprite());
            }
        }
    }
    
    private void updateEnemyBullets(long now) {
        for (Iterator<EnemyBullet> iterator = enemyBullets.iterator(); iterator.hasNext(); ) {
            EnemyBullet eb = iterator.next();
            eb.update();
            if (eb.getX() < 0 || eb.getX() > WORLD_WIDTH ||
                eb.getY() < 0 || eb.getY() > WORLD_HEIGHT) {
                worldGroup.getChildren().remove(eb.getSprite());
                iterator.remove();
                continue;
            }
            boolean hitWall = false;
            for (Rectangle wall : walls) {
                if (eb.getSprite().localToScene(eb.getSprite().getBoundsInLocal())
                    .intersects(wall.localToScene(wall.getBoundsInLocal()))) {
                    hitWall = true;
                    break;
                }
            }
            if (hitWall) {
                worldGroup.getChildren().remove(eb.getSprite());
                iterator.remove();
                continue;
            }
            if (eb.getSprite().localToScene(eb.getSprite().getBoundsInLocal())
                    .intersects(playerRect.localToScene(playerRect.getBoundsInLocal()))) {
                if (!playerInvincible) {
                    playerHealth -= eb.getDamage();
                    System.out.println("Player hit by enemy! Health: " + playerHealth);
                    playerInvincible = true;
                    invincibleStartTime = now;
                }
                eb.onHit();
                worldGroup.getChildren().remove(eb.getSprite());
                iterator.remove();
            }
        }
    }
    
    private void checkPlayerEnemyCollisions(long now) {
        if (!playerInvincible) {
            for (Enemy enemy : enemies) {
                if (enemy.getSprite().getBoundsInParent().intersects(playerRect.getBoundsInParent())) {
                    playerHealth -= enemy_damage;
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
        double offsetX = WINDOW_WIDTH / 2 - (playerX + TILE_SIZE / 2);
        double offsetY = WINDOW_HEIGHT / 2 - (playerY + TILE_SIZE / 2);
        worldGroup.setTranslateX(offsetX);
        worldGroup.setTranslateY(offsetY);
    }
    
    public double getPlayerHealth() {
        return playerHealth;
    }
}
