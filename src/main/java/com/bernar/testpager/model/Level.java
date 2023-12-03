package com.bernar.testpager.model;

public enum Level {

    LOW, MEDIUM, HIGH, CRITICAL;

    private Level nextLevel;

    static {
        LOW.nextLevel = MEDIUM;
        MEDIUM.nextLevel = HIGH;
        HIGH.nextLevel = CRITICAL;
        CRITICAL.nextLevel = CRITICAL; //cannot be escalated more
    }

    public Level getNextLevel() {
        return nextLevel;
    }
}
