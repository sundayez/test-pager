package com.bernar.testpager.domain.services;

import com.bernar.testpager.adapters.TimerAdapter;
import com.bernar.testpager.model.Timer;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class TimerManagerImpl implements TimerManager {

    private final TimerAdapter timerAdapter;
    @Override
    public void setTimer(Timer timer) {
        timerAdapter.addTimer(timer);
    }
}
