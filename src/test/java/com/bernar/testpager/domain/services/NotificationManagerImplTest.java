package com.bernar.testpager.domain.services;

import static com.bernar.testpager.TestBuilder.MESSAGE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import com.bernar.testpager.TestBuilder;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
class NotificationManagerImplTest {

    private NotificationManager notificationManager;

    @BeforeEach
    void init() {
        notificationManager = new NotificationManagerImpl();
    }

    @Test
    void When_TargetsAreNotified_Then_MailAdaptersAreCalled() {
        
        // When
        notificationManager.notifyTargets(TestBuilder.buildTargets(), MESSAGE);

        // Then
        verify(TestBuilder.mailAdapter).sendMail(anyString(), anyString());
        verify(TestBuilder.smsAdapter).sendSMS(anyString(), anyString());
    }
}
