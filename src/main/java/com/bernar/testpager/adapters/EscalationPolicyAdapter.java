package com.bernar.testpager.adapters;

import com.bernar.testpager.model.EscalationPolicy;

public interface EscalationPolicyAdapter {

    EscalationPolicy getEscalationPolicyByMonitoredService(String monitoredServiceId);

}
