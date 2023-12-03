package com.bernar.testpager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Timer {

    private String alertId;
    private Integer timeoutSeconds;
}
