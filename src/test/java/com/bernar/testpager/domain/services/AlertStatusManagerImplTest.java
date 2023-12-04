package com.bernar.testpager.domain.services;

import static com.bernar.testpager.TestBuilder.ALERT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bernar.testpager.TestBuilder;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.model.Level;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
class AlertStatusManagerImplTest {

    private AlertStatusManager alertStatusManager;

    @BeforeEach
    void init() {
        alertStatusManager = new AlertStatusManagerImpl();
    }

    @Test
    void When_AddAlertStatus_Then_StatusIsAdded() {
        // When
        alertStatusManager.addAlertStatus(TestBuilder.buildAlertStatus(Level.LOW, AckStatus.NACK));

        // Then
        assertTrue(alertStatusManager.getAlertStatusByAlertId(ALERT_ID).isPresent());
    }

    @Test
    void When_AcknowledgeAlert_Then_StatusIsAcknowledged() {
        // Given
        alertStatusManager.addAlertStatus(TestBuilder.buildAlertStatus(Level.LOW, AckStatus.NACK));

        // When
        alertStatusManager.acknowledgeAlert(ALERT_ID);

        // Then
        assertTrue(alertStatusManager.getAlertStatusByAlertId(ALERT_ID).isPresent());
        assertEquals(AckStatus.ACK, alertStatusManager.getAlertStatusByAlertId(ALERT_ID).get().getAckStatus());
    }

    @Test
    void When_UpdateLevel_Then_LevelIsUpdated() {
        // Given
        alertStatusManager.addAlertStatus(TestBuilder.buildAlertStatus(Level.LOW, AckStatus.NACK));

        // When
        alertStatusManager.updateAlertLevel(ALERT_ID, Level.MEDIUM);

        // Then
        assertTrue(alertStatusManager.getAlertStatusByAlertId(ALERT_ID).isPresent());
        assertEquals(Level.MEDIUM, alertStatusManager.getAlertStatusByAlertId(ALERT_ID).get().getLevel());
    }
}
