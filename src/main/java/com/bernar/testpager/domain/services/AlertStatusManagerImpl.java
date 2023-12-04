package com.bernar.testpager.domain.services;

import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Level;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class AlertStatusManagerImpl implements AlertStatusManager {

    private final List<AlertStatus> alertStatuses = new ArrayList<>();


    @Override
    public void addAlertStatus(AlertStatus alertStatus) {
        alertStatuses.add(alertStatus);
    }

    @Override
    public Optional<AlertStatus> getAlertStatusByAlertId(String alertId) {
        return alertStatuses.stream()
            .filter(status -> alertId.equals(status.getAlertId())).findFirst();
    }

    @Override
    public void acknowledgeAlert(String alertId) {
        alertStatuses.stream().filter(t -> alertId.equals(t.getAlertId()))
            .forEach(t -> t.setAckStatus(AckStatus.ACK));
    }

    @Override
    public void updateAlertLevel(String alertId, Level level) {
        alertStatuses.stream().filter(t -> alertId.equals(t.getAlertId()))
            .forEach(t -> t.setLevel(level));
    }
}
