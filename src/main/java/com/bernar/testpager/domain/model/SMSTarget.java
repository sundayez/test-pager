package com.bernar.testpager.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class SMSTarget extends Target {

    private String phoneNumber;

    @Override
    void sendMessage(String message) {
        log.info("SMS with message '{}' sent to phoneNumber '{}'", message, this.phoneNumber);
    }
}
