package com.bernar.testpager.domain.model;

import com.bernar.testpager.model.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertStatus {

    private String alertId;
    private Level level;
    private AckStatus ackStatus;
}
