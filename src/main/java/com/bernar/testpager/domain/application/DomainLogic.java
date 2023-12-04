package com.bernar.testpager.domain.application;

import com.bernar.testpager.model.Alert;
import com.bernar.testpager.model.Target;

public interface DomainLogic {
    void manageReceivedAlert();

    void handleTimeout(Alert alert);

    void manageReceivedAck(Target target, Alert alert);

    void manageReceivedHealthy(String monitoredServiceId);
    
}
