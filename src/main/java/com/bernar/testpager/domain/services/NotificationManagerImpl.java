package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.Target;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class NotificationManagerImpl implements NotificationManager {

    @Override
    public void notifyTargets(List<Target> targets, String message) {
        targets.forEach(target -> target.sendMessage(message));
    }
}
