package com.bernar.testpager.domain.application;

import com.bernar.testpager.domain.model.MonitoredService;
import com.bernar.testpager.domain.model.State;
import com.bernar.testpager.domain.usecases.UnhealthyUseCase;
import java.util.List;
import java.util.UUID;

public class DomainLogic {

    private UnhealthyUseCase unhealthyUseCase;

    List<MonitoredService> monitoredServices;

    public DomainLogic() {
        monitoredServices = List.of(
            MonitoredService.builder()
                .monitoredServiceId(UUID.randomUUID().toString())
                .state(State.HEALTHY)
                .build()
        );
    }

    // main logic of the Pager Service
    public void mainLogic() {

    }
}
