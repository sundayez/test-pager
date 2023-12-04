package com.bernar.testpager.model;

import com.bernar.testpager.adapters.SMSAdapter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@RequiredArgsConstructor
public class SMSTarget extends Target {

    private final String phoneNumber;

    private final SMSAdapter smsAdapter;

    @Override
    public void sendMessage(String message) {
        smsAdapter.sendSMS(this.phoneNumber, message);
    }
}
