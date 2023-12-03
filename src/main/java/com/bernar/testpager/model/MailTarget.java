package com.bernar.testpager.model;

import com.bernar.testpager.adapters.MailAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class MailTarget extends Target {

    private String emailAddress;

    private final MailAdapter mailAdapter;

    @Override
    public void sendMessage(String message) {
        mailAdapter.sendMail(this.emailAddress, message);
    }
}
