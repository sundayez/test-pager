package com.bernar.testpager.adapters;

import com.bernar.testpager.model.Target;

public interface ConsoleAdapter {

    void acknowledgeAlert(Target target, String alertId);
    void setServiceAsHealthy(String monitoredServiceId);
}
