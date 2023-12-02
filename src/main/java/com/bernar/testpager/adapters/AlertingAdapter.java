package com.bernar.testpager.adapters;

import com.bernar.testpager.domain.model.Alert;
import java.util.List;

public interface AlertingAdapter {

    List<Alert> pollAlerts(String monitoredServiceId);

}
