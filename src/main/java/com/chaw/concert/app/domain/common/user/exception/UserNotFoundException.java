package com.chaw.concert.app.domain.common.user.exception;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "잘못된 UUID 입니다";

    public UserNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
