package com.bernar.testpager.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationPolicy {

    private String monitoredServiceId;
    private Map<Level, List<Target>> levels;
}
