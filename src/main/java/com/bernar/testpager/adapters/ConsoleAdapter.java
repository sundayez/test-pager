package com.bernar.testpager.adapters;

import com.bernar.testpager.model.Target;

public interface ConsoleAdapter {

    boolean pollAcknowledgedAlert(Target target, String alertId);
    boolean pollHealthyService(String monitoredServiceId);
}
