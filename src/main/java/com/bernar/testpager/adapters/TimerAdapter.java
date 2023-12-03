package com.bernar.testpager.adapters;

import com.bernar.testpager.model.Timer;

public interface TimerAdapter {

    void addTimer(Timer timer);

    boolean isTimedOut(Timer timer);

}
