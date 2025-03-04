package com.rpg.map;

import java.util.HashMap;
import java.util.Map;

import com.rpg.gameobject.GameObjectEnum;

public class GameMap {
    // Key: Integer level; Value: TiledMap instance
    private Map<Integer, TiledMap> maps;

    public GameMap() {
        this.maps = new HashMap<>();
        initializeMaps();
    }

    private void initializeMaps() {
        // 1) Forest
        TiledMap forest = new TiledMap("Forest", 1, 20, 12);
        forest.setObjectAt(2, 2, GameObjectEnum.PLAYER);
        forest.setObjectAt(1, 7, GameObjectEnum.WALL);
        forest.setObjectAt(2, 7, GameObjectEnum.WALL);
        forest.setObjectAt(3, 7, GameObjectEnum.WALL);
        forest.setObjectAt(4, 7, GameObjectEnum.WALL);
        forest.setObjectAt(5, 7, GameObjectEnum.WALL);
        forest.setObjectAt(6, 7, GameObjectEnum.WALL);
        forest.setObjectAt(7, 7, GameObjectEnum.WALL);
        forest.setObjectAt(8, 7, GameObjectEnum.WALL);
        forest.setObjectAt(9, 7, GameObjectEnum.WALL);
        forest.setObjectAt(5, 9, GameObjectEnum.ENEMY);
        addMap(forest);

        // 2) Dungeon
        TiledMap dungeon = new TiledMap("Dungeon", 2, 15, 15);
        dungeon.setObjectAt(2, 2, GameObjectEnum.PLAYER);
        dungeon.setObjectAt(7, 7, GameObjectEnum.ENEMY);
        addMap(dungeon);

        // 3) Castle
        TiledMap castle = new TiledMap("Castle", 3, 30, 30);
        castle.setObjectAt(3, 3, GameObjectEnum.PLAYER);
        castle.setObjectAt(10, 10, GameObjectEnum.ENEMY);
        addMap(castle);
    }

    public void addMap(TiledMap map) {
        maps.put(map.getLevel(), map);
    }

    public TiledMap getMap(int level) {
        return maps.get(level);
    }

    public void displayAllMaps() {
        for (TiledMap map : maps.values()) {
            System.out.println("Map: " + map.getName() + " (Level " + map.getLevel() + ")");
            map.displayMap();
            System.out.println();
        }
    }
}

