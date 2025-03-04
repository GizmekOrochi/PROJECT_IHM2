package com.rpg.gameobject.items;

import com.rpg.gameobject.entities.Bullet;

public class Weapon {
    private double fireRate;
    private double damage;
    private int magazineSize;
    private int currentAmmo;
    private long lastShotTime;
    private double bulletSpeed;

    public Weapon(double fireRate, double damage, int magazineSize, double bulletSpeed) {
        this.fireRate = fireRate;
        this.damage = damage;
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.bulletSpeed = bulletSpeed;
        this.lastShotTime = 0;
    }
    public Bullet shoot(double startX, double startY, double targetX, double targetY, long currentTime) {
        long nanosPerShot = (long) (1_000_000_000 / fireRate);
        if (currentTime - lastShotTime < nanosPerShot) {
            return null;
        }
        if (currentAmmo <= 0) {
            return null;
        }
        lastShotTime = currentTime;
        currentAmmo--;
        return new Bullet(startX, startY, targetX, targetY, bulletSpeed, damage);
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public void reload() {
        currentAmmo = magazineSize;
    }
}

