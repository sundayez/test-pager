package com.bernar.testpager.domain.application;

import com.bernar.testpager.adapters.AlertingAdapter;
import com.bernar.testpager.adapters.ConsoleAdapter;
import com.bernar.testpager.adapters.TimerAdapter;
import com.bernar.testpager.domain.services.AlertManager;
import com.bernar.testpager.model.Alert;
import com.bernar.testpager.model.Target;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class DomainLogicApp implements DomainLogic {

    private final AlertingAdapter alertingAdapter;
    private final ConsoleAdapter consoleAdapter;
    private final TimerAdapter timerAdapter;

    private final AlertManager alertManager;

    // This method runs a single iteration of the first use case (polling and processing alerts)
    @Override
    public void manageReceivedAlert() {
        // In a real scenario, this poll could be done with a Rest API controller or a message broker consuming alert messages
        var alerts = alertingAdapter.pollAlerts();

        Map<String, List<Alert>> alertsByMonitoredService = alerts
            .stream()
            .collect(Collectors.groupingBy(Alert::getMonitoredServiceId));

        // According to the product decision in the problem definition, we only process one alert per MonitoredService
        alertsByMonitoredService.values()
            .forEach(list -> alertManager.processAlert(list.get(0)));
    }

    @Override
    public void handleTimeout() {
        // In a real scenario, this poll could be done with a Rest API controller or a message broker consuming alert messages
        var alert = Alert.builder()
            .alertId(UUID.randomUUID().toString())
            .monitoredServiceId(UUID.randomUUID().toString())
            .message("Alert message")
            .build();

        if (timerAdapter.isTimedOut(alert.getAlertId())) {
            alertManager.handleTimeout(alert);
        }
    }

    @Override
    public void manageReceivedAck(Target target, Alert alert) {
        // In a real scenario, this poll could be done with a Rest API controller
        if (consoleAdapter.pollAcknowledgedAlert(target, alert.getAlertId())) {
            alertManager.acknowledgeAlert(alert);
        }
    }

    @Override
    public void manageReceivedHealthy(String monitoredServiceId) {
        // In a real scenario, this poll could be done with a Rest API controller
        if (consoleAdapter.pollHealthyService(monitoredServiceId)) {
            alertManager.setServiceAsHealthy(monitoredServiceId);
        }
    }
}
