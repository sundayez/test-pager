package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.MonitoredService;
import com.bernar.testpager.model.State;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MonitoredServiceManagerImpl implements MonitoredServiceManager {

    List<MonitoredService> monitoredServices = new ArrayList<>();

    @Override
    public State getMonitoredServiceState(String monitoredServiceId) {
        return monitoredServices.stream()
            .filter(monitoredService -> monitoredServiceId.equals(monitoredService.getMonitoredServiceId()))
            .findFirst()
            .orElse(MonitoredService.builder().state(State.UNKNOWN).build())
            .getState();
    }

    @Override
    public void setMonitoredServiceState(String monitoredServiceId, State state) {
        var monitoredService = monitoredServices.stream()
            .filter(t -> monitoredServiceId.equals(t.getMonitoredServiceId()))
            .findFirst();
        if (monitoredService.isPresent()) {
            monitoredService.get().setState(state);
        } else {
            monitoredServices.add(
                MonitoredService.builder().monitoredServiceId(monitoredServiceId).state(state)
                    .build());
        }
    }
}
