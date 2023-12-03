package com.bernar.testpager.model;

import lombok.Getter;

@Getter
public enum Level {

    LOW, MEDIUM, HIGH, CRITICAL;

    private Level nextLevel;

    static {
        LOW.nextLevel = MEDIUM;
        MEDIUM.nextLevel = HIGH;
        HIGH.nextLevel = CRITICAL;
    }

}
