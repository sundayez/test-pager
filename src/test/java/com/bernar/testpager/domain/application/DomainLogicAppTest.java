package com.bernar.testpager.domain.application;

import static com.bernar.testpager.TestBuilder.ALERT_ID;
import static com.bernar.testpager.TestBuilder.MAIL_ADDRESS_LOW;
import static com.bernar.testpager.TestBuilder.MONITORED_SERVICE_ID;
import static com.bernar.testpager.TestBuilder.mailAdapter;
import static com.bernar.testpager.TestBuilder.smsAdapter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bernar.testpager.TestBuilder;
import com.bernar.testpager.adapters.AlertingAdapter;
import com.bernar.testpager.adapters.ConsoleAdapter;
import com.bernar.testpager.adapters.EscalationPolicyAdapter;
import com.bernar.testpager.adapters.TimerAdapter;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.domain.services.AlertManager;
import com.bernar.testpager.domain.services.AlertManagerImpl;
import com.bernar.testpager.domain.services.AlertStatusManager;
import com.bernar.testpager.domain.services.AlertStatusManagerImpl;
import com.bernar.testpager.domain.services.MonitoredServiceManager;
import com.bernar.testpager.domain.services.MonitoredServiceManagerImpl;
import com.bernar.testpager.domain.services.NotificationManager;
import com.bernar.testpager.domain.services.NotificationManagerImpl;
import com.bernar.testpager.model.Level;
import com.bernar.testpager.model.State;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class DomainLogicAppTest {

    private AlertingAdapter alertingAdapter;
    private ConsoleAdapter consoleAdapter;
    private TimerAdapter timerAdapter;
    private EscalationPolicyAdapter escalationPolicyAdapter;

    private AlertStatusManager alertStatusManager;
    private NotificationManager notificationManager;
    private MonitoredServiceManager monitoredServiceManager;
    private AlertManager alertManager;

    private DomainLogic domainLogic;

    @BeforeEach
    void init() {
        alertingAdapter = mock(AlertingAdapter.class);
        consoleAdapter = mock(ConsoleAdapter.class);
        timerAdapter = mock(TimerAdapter.class);
        escalationPolicyAdapter = mock(EscalationPolicyAdapter.class);

        alertStatusManager = new AlertStatusManagerImpl();
        notificationManager = new NotificationManagerImpl();
        monitoredServiceManager = new MonitoredServiceManagerImpl();

        alertManager = new AlertManagerImpl(
            escalationPolicyAdapter, timerAdapter, alertStatusManager, notificationManager, monitoredServiceManager);
        domainLogic = new DomainLogicApp(alertingAdapter, consoleAdapter, timerAdapter, alertManager);
    }

    @Test
    void UseCase3_EndToEnd() {
        // Given
        var target = TestBuilder.buildMailTarget(MAIL_ADDRESS_LOW);
        var alert = TestBuilder.buildAlert();
        when(alertingAdapter.pollAlerts()).thenReturn(List.of(alert));
        when(consoleAdapter.pollAcknowledgedAlert(target, alert.getAlertId())).thenReturn(true);
        when(escalationPolicyAdapter.getEscalationPolicyByMonitoredService(MONITORED_SERVICE_ID)).thenReturn(
            TestBuilder.buildEscalationPolicy());
        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // Initially the service is unhealthy and there exists a not-acknowledged alert
        monitoredServiceManager.setMonitoredServiceState(MONITORED_SERVICE_ID, State.UNHEALTHY);
        alertStatusManager.addAlertStatus(AlertStatus.builder()
            .alertId(ALERT_ID)
            .level(Level.LOW)
            .ackStatus(AckStatus.NACK)
            .build());

        // When
        domainLogic.manageReceivedAck(target, alert);
        domainLogic.handleTimeout(alert);

        // Then
        verifyNoInteractions(smsAdapter);
        verifyNoInteractions(mailAdapter);
        verify(timerAdapter, never()).addTimer(any());
    }

    @Test
    void UseCase5_EndToEnd() {
        // Given
        var alert = TestBuilder.buildAlert();
        when(consoleAdapter.pollHealthyService(MONITORED_SERVICE_ID)).thenReturn(true);
        when(timerAdapter.isTimedOut(ALERT_ID)).thenReturn(true);

        // Initially the service is unhealthy
        monitoredServiceManager.setMonitoredServiceState(MONITORED_SERVICE_ID, State.UNHEALTHY);

        // When
        domainLogic.manageReceivedHealthy(MONITORED_SERVICE_ID);
        domainLogic.handleTimeout(alert);

        // Then
        assertEquals(State.HEALTHY, monitoredServiceManager.getMonitoredServiceState(MONITORED_SERVICE_ID));
        verifyNoInteractions(smsAdapter);
        verifyNoInteractions(mailAdapter);
        verify(timerAdapter, never()).addTimer(any());
    }
}
