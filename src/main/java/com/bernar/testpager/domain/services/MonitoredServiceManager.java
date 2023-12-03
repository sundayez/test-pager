package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.State;

public interface MonitoredServiceManager {

    State getMonitoredServiceState(String monitoredServiceId);
    void setMonitoredServiceState(String monitoredServiceId, State state);

}
