package com.bernar.testpager.domain.services;

import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Level;
import java.util.Optional;

public interface AlertStatusManager {

    void addAlertStatus(AlertStatus alertStatus);

    Optional<AlertStatus> getAlertStatusByAlertId(String alertId);

    void acknowledgeAlert(String alertId);

    void updateAlertLevel(String alertId, Level level);
}
