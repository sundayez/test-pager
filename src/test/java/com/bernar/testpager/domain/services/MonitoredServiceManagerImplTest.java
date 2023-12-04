package com.bernar.testpager.domain.services;

import static com.bernar.testpager.TestBuilder.MONITORED_SERVICE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bernar.testpager.model.State;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
class MonitoredServiceManagerImplTest {

    private MonitoredServiceManager monitoredServiceManager;

    @BeforeEach
    void init() {
        monitoredServiceManager = new MonitoredServiceManagerImpl();
    }

    @Test
    void When_GetMonitoredState_Then_NoStateIsPresent() {
        assertEquals(State.UNKNOWN, monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID));
    }

    @Test
    void When_MonitoredStateIsSetForTheFirstTime_Then_StateIsPresent() {
        // Given
        monitoredServiceManager.setMonitoredServiceState(MONITORED_SERVICE_ID, State.UNHEALTHY);

        // When
        var state = monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID);

        // Then
        assertEquals(State.UNHEALTHY, state);
    }

    @Test
    void When_MonitoredStateIsUpdated_Then_StateIsOverriden() {
        // Given
        monitoredServiceManager.setMonitoredServiceState(MONITORED_SERVICE_ID, State.UNHEALTHY);
        monitoredServiceManager.setMonitoredServiceState(MONITORED_SERVICE_ID, State.HEALTHY);

        // When
        var state = monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID);

        // Then
        assertEquals(State.HEALTHY, state);
    }
}
