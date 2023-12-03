package com.bernar.testpager.domain.application;

import com.bernar.testpager.adapters.AlertingAdapter;
import com.bernar.testpager.domain.services.AlertManager;
import com.bernar.testpager.model.Alert;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class DomainLogicApp implements DomainLogic {

    private final AlertingAdapter alertingAdapter;

    private final AlertManager alertManager;

    // This is a single iteration of the application with the main logic (polling alerts and processing them)
    // In a real scenario, this could be a Rest API controller or a message broker consuming alert messages
    @Override
    public void runPagerDomainLogic() {
        var alerts = alertingAdapter.pollAlerts();

        Map<String, List<Alert>> alertsByMonitoredService = alerts
            .stream()
            .collect(Collectors.groupingBy(Alert::getMonitoredServiceId));

        // According to the product decision in the problem definition, we only process one alert per MonitoredService
        alertsByMonitoredService.values()
            .forEach(list -> alertManager.processAlert(list.get(0)));
    }
}
