package com.bernar.testpager.model;

import com.bernar.testpager.adapters.MailAdapter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@RequiredArgsConstructor
public class MailTarget extends Target {

    private final String mailAddress;

    private final MailAdapter mailAdapter;

    @Override
    public void sendMessage(String message) {
        mailAdapter.sendMail(this.mailAddress, message);
    }
}
