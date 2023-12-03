package com.bernar.testpager.domain.services;

import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Alert;
import com.bernar.testpager.model.Level;
import com.bernar.testpager.model.State;
import com.bernar.testpager.model.Timer;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class AlertManagerImpl implements AlertManager {

    private List<AlertStatus> alertStatuses = new ArrayList<>();

    private final EscalationPolicyAdapter escalationPolicyAdapter;
    private final NotificationManager notificationManager;
    private final MonitoredServiceManager monitoredServiceManager;
    private final TimerManager timerManager;

    @Override
    public void processAlert(Alert alert) {
        var monitoredServiceId = alert.getMonitoredServiceId();

        var escalationPolicy = escalationPolicyAdapter.getEscalationPolicyByMonitoredService(
            monitoredServiceId);

        monitoredServiceManager.setMonitoredServiceState(monitoredServiceId, State.UNHEALTHY);

        var targets = escalationPolicy.getLevels().get(Level.LOW);
        notificationManager.notifyTargets(targets, alert.getMessage());

        timerManager.setTimer(
            Timer.builder().alertId(alert.getAlertId()).timeoutSeconds(15 * 60).build());

        alertStatuses.add(AlertStatus.builder()
            .alertId(alert.getAlertId())
            .level(Level.LOW)
            .ackStatus(AckStatus.NACK)
            .build());
    }
}
