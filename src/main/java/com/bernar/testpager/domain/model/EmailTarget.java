package com.bernar.testpager.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class EmailTarget extends Target {

    private String emailAddress;
}
