package com.rpg.map;

import com.rpg.gameobject.GameObjectEnum;

public class TiledMap {
    private String name;
    private int level;
    private GameObjectEnum[][] grid;

    public TiledMap(String name, int level, int width, int height) {
        this.name = name;
        this.level = level;
        // grid[x][y] = ...
        this.grid = new GameObjectEnum[width][height];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                // By default: outer boundary = WALL, inside = VOID
                if (x == 0 || x == grid.length - 1 || y == 0 || y == grid[x].length - 1) {
                    grid[x][y] = GameObjectEnum.WALL;
                } else {
                    grid[x][y] = GameObjectEnum.VOID;
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    // For convenience, get total width/height in grid cells:
    public int getWidth() {
        return grid.length; // 'x' dimension
    }

    public int getHeight() {
        return grid[0].length; // 'y' dimension
    }

    public GameObjectEnum getObjectAt(int x, int y) {
        if (isValidPosition(x, y)) {
            return grid[x][y];
        }
        return null;
    }

    public void setObjectAt(int x, int y, GameObjectEnum object) {
        if (isValidPosition(x, y)) {
            grid[x][y] = object;
        }
    }

    public void displayMap() {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                System.out.print(grid[x][y] + " ");
            }
            System.out.println();
        }
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length;
    }
}

