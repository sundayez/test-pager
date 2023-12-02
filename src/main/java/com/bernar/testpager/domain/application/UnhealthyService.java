package com.bernar.testpager.domain.application;

import com.bernar.testpager.adapters.AlertingAdapter;
import com.bernar.testpager.domain.usecases.UnhealthyUseCase;

public class UnhealthyService implements UnhealthyUseCase {


    AlertingAdapter alertingAdapter;

    @Override
    public void setMonitoredServiceAsUnhealthy(String monitoredServiceId) {

    }
}
