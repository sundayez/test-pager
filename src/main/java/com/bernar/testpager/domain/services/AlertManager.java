package com.bernar.testpager.domain.services;

import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Alert;

public interface AlertManager {

//    Optional<Timer> processAlerts(MonitoredService monitoredService);

    AlertStatus processAlert(Alert alert);

}
