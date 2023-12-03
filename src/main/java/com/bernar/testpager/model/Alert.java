package com.bernar.testpager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    private String alertId;
    private String monitoredServiceId;
    private String message;
}
