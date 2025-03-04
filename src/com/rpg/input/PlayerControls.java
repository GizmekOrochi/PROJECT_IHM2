package com.rpg.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class PlayerControls {
    // Movement
    private boolean up, down, left, right;
    private boolean shooting;

    // Dodge/roll
    private boolean dodgeRequested;  // Set true if the user pressed SHIFT
    private KeyCode dodgeKey = KeyCode.SHIFT;

    private double shootX, shootY;

    private KeyCode keyUp, keyDown, keyLeft, keyRight;
    private MouseButton shootButton, reloadButton;
    private ReloadListener reloadListener;

    public PlayerControls() {
        this.keyUp = KeyCode.Z;
        this.keyDown = KeyCode.S;
        this.keyLeft = KeyCode.Q;
        this.keyRight = KeyCode.D;
        this.shootButton = MouseButton.PRIMARY;
        this.reloadButton = MouseButton.SECONDARY;
    }

    public void attachInputHandlers(Scene scene) {
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);
        scene.setOnMousePressed(this::handleMousePressed);
        scene.setOnMouseReleased(this::handleMouseReleased);
        scene.setOnMouseDragged(this::handleMouseDragged);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == keyUp || code == KeyCode.UP) {
            up = true;
        }
        if (code == keyDown || code == KeyCode.DOWN) {
            down = true;
        }
        if (code == keyLeft || code == KeyCode.LEFT) {
            left = true;
        }
        if (code == keyRight || code == KeyCode.RIGHT) {
            right = true;
        }

        // Trigger a dodge when SHIFT is pressed
        if (code == dodgeKey) {
            dodgeRequested = true;
        }
    }

    private void handleKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == keyUp || code == KeyCode.UP) {
            up = false;
        }
        if (code == keyDown || code == KeyCode.DOWN) {
            down = false;
        }
        if (code == keyLeft || code == KeyCode.LEFT) {
            left = false;
        }
        if (code == keyRight || code == KeyCode.RIGHT) {
            right = false;
        }

        // You might reset dodgeRequested here if you only want a single press
        if (code == dodgeKey) {
            // If you'd like to allow repeated dodges by releasing SHIFT, do:
            dodgeRequested = false;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == shootButton) {
            shooting = true;
            shootX = event.getSceneX();
            shootY = event.getSceneY();
        } else if (event.getButton() == reloadButton && reloadListener != null) {
            reloadListener.onReload();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == shootButton) {
            shooting = false;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (shooting && event.getButton() == shootButton) {
            shootX = event.getSceneX();
            shootY = event.getSceneY();
        }
    }

    // Movement flags
    public boolean isUp()    { return up; }
    public boolean isDown()  { return down; }
    public boolean isLeft()  { return left; }
    public boolean isRight() { return right; }

    // Shooting
    public boolean isShooting() { return shooting; }
    public double getShootX()   { return shootX; }
    public double getShootY()   { return shootY; }

    // Dodge
    public boolean isDodgeRequested() {
        return dodgeRequested;
    }
    public void setDodgeKey(KeyCode key) {
        this.dodgeKey = key;
    }

    // Provide a way for your game logic to consume this request if you like:
    public void consumeDodgeRequest() {
        dodgeRequested = false;
    }

    // Reload listener and interface
    public void setReloadListener(ReloadListener listener) {
        this.reloadListener = listener;
    }

    public interface ReloadListener {
        void onReload();
    }
}
