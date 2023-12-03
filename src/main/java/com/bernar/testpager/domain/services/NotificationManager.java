package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.Target;
import java.util.List;

public interface NotificationManager {

    void notifyTargets(List<Target> targets, String message);

}
