package com.chaw.concert.app.domain.common.user.exception;

import jakarta.persistence.EntityNotFoundException;

public class PointNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "포인트가 없습니다. 충전 후 사용해 주세요.";

    public PointNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
