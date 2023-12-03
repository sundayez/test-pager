package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.State;

public interface MonitoredServiceManager {

    void setMonitoredServiceState(String monitoredServiceId, State state);

}
