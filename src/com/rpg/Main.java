package com.rpg;

import com.rpg.view.GameView;

public class Main {
    public static void main(String[] args) {
        // Launch the game using GameLauncher, which encapsulates RPGGame and input handling.
        GameView.launch(GameView.class, args);
    }
}

