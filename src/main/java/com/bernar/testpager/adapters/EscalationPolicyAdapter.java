package com.bernar.testpager.adapters;

import com.bernar.testpager.domain.model.EscalationPolicy;

public interface EscalationPolicyAdapter {

    EscalationPolicy getEscalationPolicyByMonitoredService(String monitoredServiceId);

}
