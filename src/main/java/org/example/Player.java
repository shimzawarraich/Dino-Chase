package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.TimerTask;

class Player {
    private final String id;
    private int x, y;
    private final int size = 50;
    private final Color color;
    private boolean isChaser = false;
    private boolean wasChaser = false;
    private int tagCount = 0;
    private BufferedImage avatar;
    private boolean facingRight = true;
    private boolean recentlyTagged = false; // to protect recently un-tagged for a little while
    private long cooldownEndTime = 0;

    public Player(String id, int x, int y, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setChaser(boolean c) {
        if (c && !this.isChaser) {
            tagCount++;
            wasChaser = true;
        }
        this.isChaser = c;
    }

    public boolean isChaser() { return isChaser; }
    public int getTagCount() { return tagCount; }
    public String id() { return id; }
    public int x() { return x; }
    public int y() { return y; }
    public int size() { return size; }

    public void setAvatar(BufferedImage avatar) {
        this.avatar = avatar;
    }

    public BufferedImage getAvatar() {
        return avatar;
    }

    public boolean isRecentlyTagged() {
        return System.currentTimeMillis() < cooldownEndTime;
    }

    public void markTagged() {
        cooldownEndTime = System.currentTimeMillis() + 3000;

        // Reset after short cooldown
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                recentlyTagged = false;
            }
        }, 3000); // cooldown in milliseconds (3 sec)
    }

    public int getCooldownSecondsRemaining() {
        long diff = cooldownEndTime - System.currentTimeMillis();
        return (int) Math.ceil(diff / 1000.0);
    }

    public void setCooldownEndTime(long time) {
        this.cooldownEndTime = time;
    }

    public long getCooldownEndTime() {
        return cooldownEndTime;
    }
    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
    }
}