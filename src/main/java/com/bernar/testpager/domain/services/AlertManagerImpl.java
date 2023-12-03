package com.bernar.testpager.domain.services;

import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.adapters.TimerAdapter;
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

    private final List<AlertStatus> alertStatuses = new ArrayList<>();

    private final EscalationPolicyAdapter escalationPolicyAdapter;
    private final TimerAdapter timerAdapter;

    private final NotificationManager notificationManager;
    private final MonitoredServiceManager monitoredServiceManager;

    @Override
    public void processAlert(Alert alert) {
        var monitoredServiceId = alert.getMonitoredServiceId();

        if (State.HEALTHY.equals(
            monitoredServiceManager.getMonitoredServiceState(monitoredServiceId))) {

            monitoredServiceManager.setMonitoredServiceState(monitoredServiceId, State.UNHEALTHY);

            var escalationPolicy = escalationPolicyAdapter.getEscalationPolicyByMonitoredService(
                monitoredServiceId);

            var targets = escalationPolicy.getLevels().get(Level.LOW);
            notificationManager.notifyTargets(targets, alert.getMessage());

            timerAdapter.addTimer(
                Timer.builder().alertId(alert.getAlertId()).timeoutSeconds(15 * 60).build());

            alertStatuses.add(AlertStatus.builder()
                .alertId(alert.getAlertId())
                .level(Level.LOW)
                .ackStatus(AckStatus.NACK)
                .build());
        }
    }

    @Override
    public void handleTimeout(Alert alert) {
        var alertId = alert.getAlertId();
        if (!timerAdapter.isTimedOut(alertId)) {
            return;
        }
        var alertStatus = alertStatuses.stream()
            .filter(status -> alertId.equals(status.getAlertId())).findFirst();
        if (alertStatus.isPresent() && alertStatus.get().getAckStatus() == AckStatus.NACK
            && monitoredServiceManager.getMonitoredServiceState(alert.getMonitoredServiceId())
            == State.UNHEALTHY) {
            escalateAlert(alert, alertStatus.get());
        }
    }

    @Override
    public void acknowledgeAlert(Alert alert) {
        alertStatuses.stream().filter(t -> alert.getAlertId().equals(t.getAlertId()))
            .forEach(t -> t.setAckStatus(AckStatus.ACK));
    }

    @Override
    public void setServiceAsHealthy(String monitoredServiceId) {
        monitoredServiceManager.setMonitoredServiceState(monitoredServiceId, State.HEALTHY);
    }

    private void escalateAlert(Alert alert, AlertStatus alertStatus) {

        var escalationPolicy = escalationPolicyAdapter.getEscalationPolicyByMonitoredService(
            alert.getMonitoredServiceId());

        // If level is critical, no escalation takes place according to the problem statement
        if (!Level.CRITICAL.equals(alertStatus.getLevel())) {
            var nextLevel = alertStatus.getLevel().getNextLevel(); //LOW TO MEDIUM, etc.
            var targets = escalationPolicy.getLevels().get(nextLevel);
            notificationManager.notifyTargets(targets, alert.getMessage());

            timerAdapter.addTimer(
                Timer.builder().alertId(alert.getAlertId()).timeoutSeconds(15 * 60).build());

            // Update the level
            alertStatuses.stream().filter(t -> alert.getAlertId().equals(t.getAlertId()))
                .forEach(t -> t.setLevel(nextLevel));
        }
    }
}
