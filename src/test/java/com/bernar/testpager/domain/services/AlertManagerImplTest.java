package com.bernar.testpager.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.adapters.MailAdapter;
import com.bernar.testpager.adapters.SMSAdapter;
import com.bernar.testpager.adapters.TimerAdapter;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Alert;
import com.bernar.testpager.model.EscalationPolicy;
import com.bernar.testpager.model.Level;
import com.bernar.testpager.model.MailTarget;
import com.bernar.testpager.model.SMSTarget;
import com.bernar.testpager.model.State;
import com.bernar.testpager.model.Target;
import com.bernar.testpager.model.Timer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.List;
import java.util.Map;
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

    private static final String ALERT_ID = "ALERT_ID";
    private static final String MONITORED_SERVICE_ID = "MONITORED_SERVICE_ID";
    private static final String MESSAGE = "MESSAGE";

    private static final String PHONE_NUMBER_LOW = "555-1234";
    private static final String PHONE_NUMBER_MEDIUM = "555-1235";
    private static final String PHONE_NUMBER_HIGH = "555-1236";
    private static final String PHONE_NUMBER_CRITICAL = "555-1237";

    private static final String MAIL_ADDRESS_LOW = "low@mail.com";
    private static final String MAIL_ADDRESS_MEDIUM = "medium@mail.com";
    private static final String MAIL_ADDRESS_HIGH = "high@mail.com";
    private static final String MAIL_ADDRESS_CRITICAL = "critical@mail.com";

    private EscalationPolicyAdapter escalationPolicyAdapter;
    private TimerAdapter timerAdapter;

    private AlertStatusManager alertStatusManager;
    private NotificationManager notificationManager;
    private MonitoredServiceManager monitoredServiceManager;

    private SMSAdapter smsAdapter;
    private MailAdapter mailAdapter;

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
        smsAdapter = mock(SMSAdapter.class);
        mailAdapter = mock(MailAdapter.class);

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
            .thenReturn(buildEscalationPolicy());

        // When
        alertManager.processAlert(buildAlert());

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
            Optional.of(buildAlertStatus(level, AckStatus.NACK)));

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        when(escalationPolicyAdapter.getEscalationPolicyByMonitoredService(MONITORED_SERVICE_ID))
            .thenReturn(buildEscalationPolicy());

        // When
        alertManager.handleTimeout(buildAlert());

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
    void UseCase2_When_TimeoutReceived_And_HealthyService_Then_NoEscalation() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.HEALTHY);

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // When
        alertManager.handleTimeout(buildAlert());

        // Then
        verifyNoInteractions(notificationManager);
        verify(timerAdapter, never()).addTimer(any());
        verifyNoInteractions(alertStatusManager);
    }

    @Test
    void UseCase2_When_TimeoutReceived_And_AcknowledgedAlert_Then_NoEscalation() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.UNHEALTHY);

        when(alertStatusManager.getAlertStatusByAlertId(ALERT_ID)).thenReturn(
            Optional.of(buildAlertStatus(Level.LOW, AckStatus.ACK)));

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // When
        alertManager.handleTimeout(buildAlert());

        // Then
        verifyNoInteractions(notificationManager);
        verify(timerAdapter, never()).addTimer(any());
        verify(alertStatusManager, never()).updateAlertLevel(anyString(), any());
    }

    @Test
    void UseCase2_When_TimeoutReceived_And_LastLevelOfEscalation_Then_NoEscalation() {
        // Given
        when(monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID)).thenReturn(
            State.UNHEALTHY);

        when(alertStatusManager.getAlertStatusByAlertId(ALERT_ID)).thenReturn(
            Optional.of(buildAlertStatus(Level.CRITICAL, AckStatus.NACK)));

        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // When
        alertManager.handleTimeout(buildAlert());

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
        alertManager.processAlert(buildAlert());

        // Then
        verify(monitoredServiceManager, never()).setMonitoredServiceState(anyString(), any());
        verifyNoInteractions(notificationManager);
        verifyNoInteractions(timerAdapter);
        verifyNoInteractions(alertStatusManager);
    }

    private Alert buildAlert() {
        return Alert.builder()
            .alertId(ALERT_ID)
            .monitoredServiceId(MONITORED_SERVICE_ID)
            .message(MESSAGE)
            .build();
    }

    private AlertStatus buildAlertStatus(Level level, AckStatus ackStatus) {
        return AlertStatus.builder()
            .alertId(ALERT_ID)
            .ackStatus(ackStatus)
            .level(level)
            .build();
    }

    private EscalationPolicy buildEscalationPolicy() {
        return EscalationPolicy.builder()
            .monitoredServiceId(MONITORED_SERVICE_ID)
            .levels(Map.of(
                Level.LOW,
                List.of(buildSMSTarget(PHONE_NUMBER_LOW), buildMailTarget(MAIL_ADDRESS_LOW)),
                Level.MEDIUM,
                List.of(buildSMSTarget(PHONE_NUMBER_MEDIUM), buildMailTarget(MAIL_ADDRESS_MEDIUM)),
                Level.HIGH,
                List.of(buildSMSTarget(PHONE_NUMBER_HIGH), buildMailTarget(MAIL_ADDRESS_HIGH)),
                Level.CRITICAL, List.of(buildSMSTarget(PHONE_NUMBER_CRITICAL),
                    buildMailTarget(MAIL_ADDRESS_CRITICAL))
            ))
            .build();
    }

    private SMSTarget buildSMSTarget(String phoneNumber) {
        return SMSTarget.builder()
            .smsAdapter(smsAdapter)
            .phoneNumber(phoneNumber)
            .build();
    }

    private MailTarget buildMailTarget(String mailAddress) {
        return MailTarget.builder()
            .mailAdapter(mailAdapter)
            .mailAddress(mailAddress)
            .build();
    }

    private static Stream<Arguments> providerTestEscalation() {
        return Stream.of(
            Arguments.of(Level.LOW, Level.MEDIUM, PHONE_NUMBER_MEDIUM, MAIL_ADDRESS_MEDIUM),
            Arguments.of(Level.MEDIUM, Level.HIGH, PHONE_NUMBER_HIGH, MAIL_ADDRESS_HIGH),
            Arguments.of(Level.HIGH, Level.CRITICAL, PHONE_NUMBER_CRITICAL, MAIL_ADDRESS_CRITICAL)
        );
    }
}
