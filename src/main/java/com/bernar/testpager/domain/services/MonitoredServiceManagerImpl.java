package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.State;
import com.bernar.testpager.model.Target;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Singleton
public class MonitoredServiceManagerImpl implements MonitoredServiceManager {

    @Override
    public void setMonitoredServiceState(String monitoredServiceId, State state) {

    }
}
