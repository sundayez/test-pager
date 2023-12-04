package com.bernar.testpager.domain.services;

import static com.bernar.testpager.TestBuilder.ALERT_ID;
import static com.bernar.testpager.TestBuilder.MAIL_ADDRESS_CRITICAL;
import static com.bernar.testpager.TestBuilder.MAIL_ADDRESS_HIGH;
import static com.bernar.testpager.TestBuilder.MAIL_ADDRESS_LOW;
import static com.bernar.testpager.TestBuilder.MAIL_ADDRESS_MEDIUM;
import static com.bernar.testpager.TestBuilder.MESSAGE;
import static com.bernar.testpager.TestBuilder.MONITORED_SERVICE_ID;
import static com.bernar.testpager.TestBuilder.PHONE_NUMBER_CRITICAL;
import static com.bernar.testpager.TestBuilder.PHONE_NUMBER_HIGH;
import static com.bernar.testpager.TestBuilder.PHONE_NUMBER_LOW;
import static com.bernar.testpager.TestBuilder.PHONE_NUMBER_MEDIUM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bernar.testpager.TestBuilder;
import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.adapters.TimerAdapter;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Level;
import com.bernar.testpager.model.MailTarget;
import com.bernar.testpager.model.SMSTarget;
import com.bernar.testpager.model.State;
import com.bernar.testpager.model.Target;
import com.bernar.testpager.model.Timer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

@MicronautTest
class AlertManagerImplTest {

    private EscalationPolicyAdapter escalationPolicyAdapter;
    private TimerAdapter timerAdapter;

    private AlertStatusManager alertStatusManager;
    private NotificationManager notificationManager;
    private MonitoredServiceManager monitoredServiceManager;

    private ArgumentCaptor<List<Target>> listArgumentCaptor;
    private ArgumentCaptor<Timer> timerArgumentCaptor;
    private ArgumentCaptor<AlertStatus> alertStatusArgumentCaptor;
    private AlertManager alertManager;

    @BeforeEach
    void init() {
        escalationPolicyAdapter = mock(EscalationPolicyAdapter.class);
        timerAdapter = mock(TimerAdapter.class);
        alertStatusManager = mock(AlertStatusManager.class);
        notificationManager = mock(NotificationManager.class);
        monitoredServiceManager = mock(MonitoredServiceManager.class);

        listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        timerArgumentCaptor = ArgumentCaptor.forClass(Timer.class);
        alertStatusArgumentCaptor = ArgumentCaptor.forClass(AlertStatus.class);
        alertManager = new AlertManagerImpl(escalationPolicyAdapter, timerAdapter,
            alertStatusManager,
            notificationManager, monitoredServiceManager);
    }

    @Test
    void UseCase1_When_PagerReceivesAlert_Then_ServiceUnhealthy_And_TargetsNotified_And_TimerCreated() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.HEALTHY);

        when(escalationPolicyAdapter.getEscalationPolicyByMonitoredService(MONITORED_SERVICE_ID))
            .thenReturn(TestBuilder.buildEscalationPolicy());

        // When
        alertManager.processAlert(TestBuilder.buildAlert());

        // Then
        verify(monitoredServiceManager).setMonitoredServiceState(MONITORED_SERVICE_ID,
            State.UNHEALTHY);

        verify(notificationManager).notifyTargets(listArgumentCaptor.capture(), eq(MESSAGE));
        var targets = listArgumentCaptor.getValue();
        assertEquals(2, targets.size());
        assertEquals(PHONE_NUMBER_LOW, ((SMSTarget) targets.get(0)).getPhoneNumber());
        assertEquals(MAIL_ADDRESS_LOW, ((MailTarget) targets.get(1)).getMailAddress());

        verify(timerAdapter).addTimer(timerArgumentCaptor.capture());
        assertEquals(ALERT_ID, timerArgumentCaptor.getValue().getAlertId());

        verify(alertStatusManager).addAlertStatus(alertStatusArgumentCaptor.capture());
        var alertStatus = alertStatusArgumentCaptor.getValue();
        assertEquals(ALERT_ID, alertStatus.getAlertId());
        assertEquals(Level.LOW, alertStatus.getLevel());
        assertEquals(AckStatus.NACK, alertStatus.getAckStatus());
    }

    @ParameterizedTest(name = "Escalation from {0}")
    @MethodSource("providerTestEscalation")
    void UseCase2_When_TimeoutReceivedForUnhealthyService_And_AlertIsNotAcknowledged_And_LastLevelIsNotReached_Then_AlertIsEscalated(
        Level level, Level nextLevel, String phoneNumber, String mailAddress
    ) {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.UNHEALTHY);

        when(alertStatusManager.getAlertStatusByAlertId(ALERT_ID)).thenReturn(
            Optional.of(TestBuilder.buildAlertStatus(level, AckStatus.NACK)));

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        when(escalationPolicyAdapter.getEscalationPolicyByMonitoredService(MONITORED_SERVICE_ID))
            .thenReturn(TestBuilder.buildEscalationPolicy());

        // When
        alertManager.handleTimeout(TestBuilder.buildAlert());

        // Then
        verify(notificationManager).notifyTargets(listArgumentCaptor.capture(), eq(MESSAGE));
        var targets = listArgumentCaptor.getValue();
        assertEquals(2, targets.size());
        assertEquals(phoneNumber, ((SMSTarget) targets.get(0)).getPhoneNumber());
        assertEquals(mailAddress, ((MailTarget) targets.get(1)).getMailAddress());

        verify(timerAdapter).addTimer(timerArgumentCaptor.capture());
        assertEquals(ALERT_ID, timerArgumentCaptor.getValue().getAlertId());

        verify(alertStatusManager).updateAlertLevel(ALERT_ID, nextLevel);
    }

    @Test
    void UseCase2_When_TimeoutReceived_And_LastLevelOfEscalation_Then_NoEscalation() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.UNHEALTHY);

        when(alertStatusManager.getAlertStatusByAlertId(ALERT_ID)).thenReturn(
            Optional.of(TestBuilder.buildAlertStatus(Level.CRITICAL, AckStatus.NACK)));

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // When
        alertManager.handleTimeout(TestBuilder.buildAlert());

        // Then
        verifyNoInteractions(notificationManager);
        verify(timerAdapter, never()).addTimer(any());
        verify(alertStatusManager, never()).updateAlertLevel(anyString(), any());
    }

    @Test
    void UseCase3_Partial_When_AlertIsAcknowledged_And_LaterTimeOut_Then_NoEscalation() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.UNHEALTHY);

        when(alertStatusManager.getAlertStatusByAlertId(ALERT_ID)).thenReturn(
            Optional.of(TestBuilder.buildAlertStatus(Level.LOW, AckStatus.ACK)));

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // When
        alertManager.handleTimeout(TestBuilder.buildAlert());

        // Then
        verifyNoInteractions(notificationManager);
        verify(timerAdapter, never()).addTimer(any());
        verify(alertStatusManager, never()).updateAlertLevel(anyString(), any());
    }

    @Test
    void UseCase4_When_PagerReceivesAlertForUnhealthy_Then_NothingHappens() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.UNHEALTHY);

        // When
        alertManager.processAlert(TestBuilder.buildAlert());

        // Then
        verify(monitoredServiceManager, never()).setMonitoredServiceState(anyString(), any());
        verifyNoInteractions(notificationManager);
        verifyNoInteractions(timerAdapter);
        verifyNoInteractions(alertStatusManager);
    }

    @Test
    void UseCase5_Partial_When_TimeoutReceived_And_HealthyService_Then_NoEscalation() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.HEALTHY);

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // When
        alertManager.handleTimeout(TestBuilder.buildAlert());

        // Then
        verifyNoInteractions(notificationManager);
        verify(timerAdapter, never()).addTimer(any());
        verifyNoInteractions(alertStatusManager);
    }

    @Test
    void When_AcknowledgeAlert_Then_StatusIsAcknowledged() {
        // When
        alertManager.acknowledgeAlert(TestBuilder.buildAlert());

        // Then
        verify(alertStatusManager).acknowledgeAlert(ALERT_ID);
    }

    @Test
    void When_ServiceBecomesHealthy_Then_StatusIsUpdated() {
        // When
        alertManager.setServiceAsHealthy(MONITORED_SERVICE_ID);

        // Then
        verify(monitoredServiceManager).setMonitoredServiceState(MONITORED_SERVICE_ID, State.HEALTHY);
    }

    private static Stream<Arguments> providerTestEscalation() {
        return Stream.of(
            Arguments.of(Level.LOW, Level.MEDIUM, PHONE_NUMBER_MEDIUM, MAIL_ADDRESS_MEDIUM),
            Arguments.of(Level.MEDIUM, Level.HIGH, PHONE_NUMBER_HIGH, MAIL_ADDRESS_HIGH),
            Arguments.of(Level.HIGH, Level.CRITICAL, PHONE_NUMBER_CRITICAL, MAIL_ADDRESS_CRITICAL)
        );
    }
}
