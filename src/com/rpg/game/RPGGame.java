package com.rpg.game;

import com.rpg.config.GameConfig;
import com.rpg.gameobject.GameObjectEnum;
import com.rpg.gameobject.entities.Bullet;
import com.rpg.gameobject.entities.Enemy;
import com.rpg.gameobject.entities.EnemyBullet;
import com.rpg.gameobject.items.Weapon;
import com.rpg.map.GameMap;
import com.rpg.map.TiledMap;
import javafx.scene.Group;
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

    private double worldWidth;
    private double worldHeight;

    private static final double PLAYER_SPEED = GameConfig.PLAYER_SPEED;
    private double playerX, playerY;
    private double playerHealth = GameConfig.PLAYER_HEALTH;
    private Rectangle playerRect;

    private final List<Rectangle> walls = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();

    private Group worldGroup;

    private Weapon playerWeapon;

    // Damage / invincibility
    private static final double ENEMY_DAMAGE = GameConfig.ENEMY_DAMAGE;
    private boolean playerInvincible = false;
    private long invincibleStartTime = 0;
    private static final long INVINCIBLE_DURATION = GameConfig.INVINCIBLE_DURATION;

    // Dodge parameters
    private static final double DODGE_SPEED_MULTIPLIER = GameConfig.DODGE_SPEED_MULTIPLIER;
    private static final long DODGE_DURATION = GameConfig.DODGE_DURATION;
    private static final long DODGE_COOLDOWN = GameConfig.DODGE_COOLDOWN;  // 5-second cooldown
    private long nextDodgeAllowedTime = 0;

    // Dodge state
    private boolean dodging = false;
    private long dodgeStartTime = 0;

    // Map & level
    private final GameMap gameMap;
    private TiledMap currentMap;

    // ----- Input flags set externally by GameLauncher -----
    private boolean moveUp, moveDown, moveLeft, moveRight, dodgeRequested;

    public RPGGame(GameMap gameMap) {
        this.gameMap = gameMap;
        this.currentMap = gameMap.getMap(1);
    }

    public Pane initialize() {
        Pane root = createRootPane();

        // Create a group for the game world
        worldGroup = new Group();
        root.getChildren().add(worldGroup);

        // Generate map tiles, player, and enemies
        generateTiles();
        createPlayer();
        spawnEnemies();

        if (playerRect != null) {
            worldGroup.getChildren().add(playerRect);
        }

        if (currentMap != null) {
            this.worldWidth = currentMap.getWidth() * TILE_SIZE;
            this.worldHeight = currentMap.getHeight() * TILE_SIZE;
        }

        // Create player's weapon and set up reload listener if needed
        playerWeapon = new Weapon(
                GameConfig.PLAYER_SHOOT_INTERVAL,
                GameConfig.PLAYER_BULLET_DAMAGE,
                GameConfig.PLAYER_AMMO_CAPACITY,
                GameConfig.PLAYER_BULLET_SPEED
        );

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

    // ----- Methods called by external input handlers -----

    public void onMoveUp(boolean pressed) {
        moveUp = pressed;
    }

    public void onMoveDown(boolean pressed) {
        moveDown = pressed;
    }

    public void onMoveLeft(boolean pressed) {
        moveLeft = pressed;
    }

    public void onMoveRight(boolean pressed) {
        moveRight = pressed;
    }

    public void onDodgeRequest() {
        dodgeRequested = true;
    }

    /**
     * Called externally when shooting input is detected.
     * @param sceneX The x coordinate of the mouse in scene space.
     * @param sceneY The y coordinate of the mouse in scene space.
     * @param now The current time.
     */
    public void onShoot(double sceneX, double sceneY, long now) {
        // Convert scene coordinates to world coordinates
        double worldX = sceneX - worldGroup.getTranslateX();
        double worldY = sceneY - worldGroup.getTranslateY();
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

    /**
     * Called externally to reload the player's weapon.
     */
    public void reloadWeapon() {
        playerWeapon.reload();
        System.out.println("Weapon reloaded.");
    }

    // ----- External update method -----
    public void update(long now) {
        updatePlayer(now);
        updateEnemies();
        updateBullets();
        updateEnemyShooting(now);
        updateEnemyBullets(now);
        updatePlayerInvincibility(now);
        updateWorldOffset();
        checkPlayerEnemyCollisions(now);
    }

    private void updatePlayer(long now) {
        double dx = 0, dy = 0;
        if (moveUp)    dy -= PLAYER_SPEED;
        if (moveDown)  dy += PLAYER_SPEED;
        if (moveLeft)  dx -= PLAYER_SPEED;
        if (moveRight) dx += PLAYER_SPEED;

        if (dodgeRequested && !dodging && now >= nextDodgeAllowedTime) {
            startDodge(now);
            dodgeRequested = false;
        }

        if (dodging) {
            long elapsed = now - dodgeStartTime;
            if (elapsed < DODGE_DURATION) {
                dx *= DODGE_SPEED_MULTIPLIER;
                dy *= DODGE_SPEED_MULTIPLIER;
            } else {
                endDodge(now);
            }
        }

        double newX = playerX + dx;
        double newY = playerY + dy;

        if (!collides(newX, playerY)) {
            playerX = newX;
        }
        if (!collides(playerX, newY)) {
            playerY = newY;
        }
        playerX = clamp(playerX, 0, worldWidth - TILE_SIZE);
        playerY = clamp(playerY, 0, worldHeight - TILE_SIZE);

        playerRect.setX(playerX);
        playerRect.setY(playerY);
    }

    private void startDodge(long now) {
        dodging = true;
        dodgeStartTime = now;
        playerInvincible = true;
        System.out.println("Player started dodging at " + now);
    }

    private void endDodge(long now) {
        dodging = false;
        playerInvincible = false;
        nextDodgeAllowedTime = now + DODGE_COOLDOWN;
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.update(playerX, playerY, currentMap, enemies);
        }
    }

    private void updateBullets() {
        for (Iterator<Bullet> it = bullets.iterator(); it.hasNext();) {
            Bullet bullet = it.next();
            bullet.update();
            if (bullet.getX() < 0 || bullet.getX() > worldWidth ||
                bullet.getY() < 0 || bullet.getY() > worldHeight) {
                worldGroup.getChildren().remove(bullet.getSprite());
                it.remove();
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
                it.remove();
                continue;
            }
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

