package com.bernar.testpager.domain.services;

import com.bernar.testpager.domain.model.MonitoredService;

public interface AlertProcessor {

    void processAlerts(MonitoredService monitoredService);

}
