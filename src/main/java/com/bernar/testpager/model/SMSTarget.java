package com.bernar.testpager.model;

import com.bernar.testpager.adapters.SMSAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class SMSTarget extends Target {

    private String phoneNumber;

    private final SMSAdapter smsAdapter;

    @Override
    public void sendMessage(String message) {
        smsAdapter.sendSMS(this.phoneNumber, message);
    }
}
