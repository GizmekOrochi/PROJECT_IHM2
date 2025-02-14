package com.rpg.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class PlayerControls {
    // Movement flags
    private boolean up, down, left, right;
    // Shooting flag for continuous shooting
    private boolean shooting;
    // Coordinates for shooting target (scene coordinates)
    private double shootX, shootY;
    
    // Configurable key bindings for movement.
    private KeyCode keyUp;
    private KeyCode keyDown;
    private KeyCode keyLeft;
    private KeyCode keyRight;
    
    // Configurable mouse button assignments.
    // Default: right-click to shoot, left-click to reload.
    private MouseButton shootButton;
    private MouseButton reloadButton;
    
    // Listener for reload events.
    private ReloadListener reloadListener;

    // Constructor: sets default key and mouse button bindings.
    public PlayerControls() {
        keyUp = KeyCode.Z;
        keyDown = KeyCode.S;
        keyLeft = KeyCode.Q;
        keyRight = KeyCode.D;
        shootButton = MouseButton.PRIMARY; // left-click for shooting
        reloadButton = MouseButton.SECONDARY;  // right-click for reload
        shooting = false;
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
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == shootButton) {
            // Start shooting (continuous) and record the target coordinates.
            shooting = true;
            shootX = event.getSceneX();
            shootY = event.getSceneY();
        } else if (event.getButton() == reloadButton) {
            // Left-click triggers reload.
            if (reloadListener != null) {
                reloadListener.onReload();
            }
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        if (event.getButton() == shootButton) {
            // Stop shooting.
            shooting = false;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        // If shooting is active, update the target coordinates.
        if (shooting && event.getButton() == shootButton) {
            shootX = event.getSceneX();
            shootY = event.getSceneY();
        }
    }

    // Getters for movement flags.
    public boolean isUp() { return up; }
    public boolean isDown() { return down; }
    public boolean isLeft() { return left; }
    public boolean isRight() { return right; }

    // Getter for shooting flag and target coordinates.
    public boolean isShooting() { return shooting; }
    public double getShootX() { return shootX; }
    public double getShootY() { return shootY; }

    // Setter methods for customizable key bindings.
    public void setKeyUp(KeyCode key) { this.keyUp = key; }
    public void setKeyDown(KeyCode key) { this.keyDown = key; }
    public void setKeyLeft(KeyCode key) { this.keyLeft = key; }
    public void setKeyRight(KeyCode key) { this.keyRight = key; }

    // Setter methods for mouse button assignments.
    public void setShootButton(MouseButton button) { this.shootButton = button; }
    public void setReloadButton(MouseButton button) { this.reloadButton = button; }

    // Setter for the reload listener.
    public void setReloadListener(ReloadListener listener) {
        this.reloadListener = listener;
    }

    // Reload listener interface.
    public interface ReloadListener {
        void onReload();
    }
}

