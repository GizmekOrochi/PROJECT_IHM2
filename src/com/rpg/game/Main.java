package com.rpg.game;

import com.rpg.launcher.GameLauncher;

public class Main {
    public static void main(String[] args) {
        // Launch the game using GameLauncher, which encapsulates RPGGame and input handling.
        GameLauncher.launch(GameLauncher.class, args);
    }
}

