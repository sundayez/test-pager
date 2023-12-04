package com.bernar.testpager;

import static org.mockito.Mockito.mock;

import com.bernar.testpager.adapters.MailAdapter;
import com.bernar.testpager.adapters.SMSAdapter;
import com.bernar.testpager.domain.model.AckStatus;
import com.bernar.testpager.domain.model.AlertStatus;
import com.bernar.testpager.model.Alert;
import com.bernar.testpager.model.EscalationPolicy;
import com.bernar.testpager.model.Level;
import com.bernar.testpager.model.MailTarget;
import com.bernar.testpager.model.SMSTarget;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestBuilder {

    public static final String ALERT_ID = "ALERT_ID";
    public static final String MONITORED_SERVICE_ID = "MONITORED_SERVICE_ID";
    public static final String MESSAGE = "MESSAGE";

    public static final String PHONE_NUMBER_LOW = "555-1234";
    public static final String PHONE_NUMBER_MEDIUM = "555-1235";
    public static final String PHONE_NUMBER_HIGH = "555-1236";
    public static final String PHONE_NUMBER_CRITICAL = "555-1237";

    public static final String MAIL_ADDRESS_LOW = "low@mail.com";
    public static final String MAIL_ADDRESS_MEDIUM = "medium@mail.com";
    public static final String MAIL_ADDRESS_HIGH = "high@mail.com";
    public static final String MAIL_ADDRESS_CRITICAL = "critical@mail.com";

    public static final SMSAdapter smsAdapter = mock(SMSAdapter.class);
    public static final MailAdapter mailAdapter = mock(MailAdapter.class);

    public Alert buildAlert() {
        return Alert.builder()
            .alertId(ALERT_ID)
            .monitoredServiceId(MONITORED_SERVICE_ID)
            .message(MESSAGE)
            .build();
    }

    public AlertStatus buildAlertStatus(Level level, AckStatus ackStatus) {
        return AlertStatus.builder()
            .alertId(ALERT_ID)
            .ackStatus(ackStatus)
            .level(level)
            .build();
    }

    public EscalationPolicy buildEscalationPolicy() {
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

    public SMSTarget buildSMSTarget(String phoneNumber) {
        return SMSTarget.builder()
            .smsAdapter(smsAdapter)
            .phoneNumber(phoneNumber)
            .build();
    }

    public MailTarget buildMailTarget(String mailAddress) {
        return MailTarget.builder()
            .mailAdapter(mailAdapter)
            .mailAddress(mailAddress)
            .build();
    }



}
