package com.bernar.testpager.domain.services;

import com.bernar.testpager.model.Alert;

public interface AlertManager {

    void processAlert(Alert alert);

    void handleTimeout(Alert alert);

    void acknowledgeAlert(Alert alert);

    void setServiceAsHealthy(String monitoredServiceId);
}
