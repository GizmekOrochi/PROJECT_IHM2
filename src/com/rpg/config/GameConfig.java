package com.rpg.config;

public class GameConfig {
    //Game settings
    public static final double TARGET_FPS = 60.0;
    public static final long OPTIMAL_TIME = 1_000_000_000L / (long) TARGET_FPS;
    //Window and World settings.
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    public static final int TILE_SIZE = 40;
    public static final int WORLD_WIDTH = 2600;
    public static final int WORLD_HEIGHT = 1200;
    
    //Player settings.
    public static final double PLAYER_SPEED = 6.0;
    public static final double PLAYER_HEALTH = 100.0;
    
    //Enemy settings.
    public static final double ENEMY_HEALTH = 100.0;
    public static final double ENEMY_SIZE = 40;
    public static final double ENEMY_SPEED = 2.0;
    public static final int ENEMY_RANGE = 160;
    public static final double ENEMY_DAMAGE = 0.5;
    
    //Enemy shooting settings.
    public static final long ENEMY_SHOOT_INTERVAL = 1_000_000_000L;
    public static final double ENEMY_BULLET_SPEED = 5.0;
    public static final double ENEMY_BULLET_DAMAGE = 10.0;
    public static final double ENEMY_SHOOTING_RANGE = 400.0;
    
    //Player weapon settings.
    public static final double PLAYER_SHOOT_INTERVAL = 2.0;
    public static final double PLAYER_BULLET_SPEED = 8.0;
    public static final double PLAYER_BULLET_DAMAGE = 20.0;
    public static final int PLAYER_AMMO_CAPACITY = 10;
    public static final double PLAYER_RELOAD_TIME = 2.0;

    //Key bindings
    public static String KEY_UP ="Z";
    public static String KEY_DOWN ="S";
    public static String KEY_LEFT ="Q";
    public static String KEY_RIGHT ="D";
}

