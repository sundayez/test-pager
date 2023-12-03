package com.bernar.testpager.domain.services;

import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Alert;
import com.bernar.testpager.model.Level;
import com.bernar.testpager.model.State;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class AlertManagerImpl implements AlertManager {


    private final EscalationPolicyAdapter escalationPolicyAdapter;

    private final NotificationManager notificationManager;

    private final MonitoredServiceManager monitoredServiceManager;

//    @Override
//    public Optional<Timer> processAlerts(MonitoredService monitoredService) {
//        var monitoredServiceId = monitoredService.getMonitoredServiceId();
//        var alerts = alertingAdapter.pollAlerts(); //TODO remove
//
//        // Only one notification is sent when several alerts are received at the same time
//        if (!alerts.isEmpty()) {
//            // The message sent is the first alert's one
//            var alert = alerts.get(0);
//            monitoredService.setState(State.UNHEALTHY);
//            var escalationPolicy = escalationPolicyAdapter.getEscalationPolicyByMonitoredService(
//                monitoredServiceId);
//            var targets = escalationPolicy.getLevels().get(FIRST_LEVEL);
//            targets.forEach(target -> target.sendMessage(alert.getMessage()));
//            return Optional.of(
//                Timer.builder().alertId(alert.getAlertId()).timeoutSeconds(15 * 60).build());
//        }
//        return Optional.empty();
//    }

    @Override
    public AlertStatus processAlert(Alert alert) {
        var monitoredServiceId = alert.getMonitoredServiceId();

        var escalationPolicy = escalationPolicyAdapter.getEscalationPolicyByMonitoredService(monitoredServiceId);

        monitoredServiceManager.setMonitoredServiceState(monitoredServiceId, State.UNHEALTHY);

        var targets = escalationPolicy.getLevels().get(Level.LOW);
        notificationManager.notifyTargets(targets, alert.getMessage());

        return AlertStatus.builder()
            .alertId(alert.getAlertId())
            .level(Level.LOW)
            .ackStatus(AckStatus.NACK)
            .build();

    }
}
