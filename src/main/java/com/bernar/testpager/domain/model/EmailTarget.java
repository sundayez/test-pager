package com.bernar.testpager.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class EmailTarget extends Target {

    private String emailAddress;

    @Override
    void sendMessage(String message) {
        log.info("Mail with message '{}' sent to emailAddress '{}'", message, this.emailAddress);
    }
}
