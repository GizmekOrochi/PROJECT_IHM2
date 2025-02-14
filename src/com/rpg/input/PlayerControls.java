package com.rpg.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class PlayerControls {
    private boolean up, down, left, right;
    private boolean shooting;
    private double shootX, shootY;
    
    private KeyCode keyUp;
    private KeyCode keyDown;
    private KeyCode keyLeft;
    private KeyCode keyRight;
    
    private MouseButton shootButton;
    private MouseButton reloadButton;
    
    private ReloadListener reloadListener;

    public PlayerControls(String up,String down,String left,String right) {
        keyUp = KeyCode.valueOf(up);
        keyDown = KeyCode.valueOf(down);
        keyLeft = KeyCode.valueOf(left);
        keyRight = KeyCode.valueOf(right);
        shootButton = MouseButton.PRIMARY;
        reloadButton = MouseButton.SECONDARY;
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
            shooting = true;
            shootX = event.getSceneX();
            shootY = event.getSceneY();
        } else if (event.getButton() == reloadButton) {
            if (reloadListener != null) {
                reloadListener.onReload();
            }
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

    public boolean isUp() { return up; }
    public boolean isDown() { return down; }
    public boolean isLeft() { return left; }
    public boolean isRight() { return right; }

    public boolean isShooting() { return shooting; }
    public double getShootX() { return shootX; }
    public double getShootY() { return shootY; }

    public void setKeyUp(KeyCode key) { this.keyUp = key; }
    public void setKeyDown(KeyCode key) { this.keyDown = key; }
    public void setKeyLeft(KeyCode key) { this.keyLeft = key; }
    public void setKeyRight(KeyCode key) { this.keyRight = key; }

    public void setShootButton(MouseButton button) { this.shootButton = button; }
    public void setReloadButton(MouseButton button) { this.reloadButton = button; }

    public void setReloadListener(ReloadListener listener) {
        this.reloadListener = listener;
    }

    public interface ReloadListener {
        void onReload();
    }
}

