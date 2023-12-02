package com.bernar.testpager.domain.services;

import com.bernar.testpager.adapters.AlertingAdapter;
import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.domain.model.MonitoredService;
import com.bernar.testpager.domain.model.State;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class AlertProcessorImpl implements AlertProcessor {


    public static final int FIRST_LEVEL = 0;
    
    private final AlertingAdapter alertingAdapter;
    private final EscalationPolicyAdapter escalationPolicyAdapter;

    @Override
    public void processAlerts(MonitoredService monitoredService) {
        var monitoredServiceId = monitoredService.getMonitoredServiceId();
        var alerts = alertingAdapter.pollAlerts(monitoredServiceId);

        // Only one notification is sent when several alerts are received at the same time
        if (!alerts.isEmpty()) {
            monitoredService.setState(State.UNHEALTHY);
            var escalationPolicy = escalationPolicyAdapter.getEscalationPolicyByMonitoredService(
                monitoredServiceId);
            var targets = escalationPolicy.getLevels().get(FIRST_LEVEL);
            targets.forEach(target -> target.sendMessage(alerts.get(0).getMessage()));
        }
    }
}
