package com.chaw.concert.app.domain.common.user.exception;

public class PointNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "포인트가 없습니다. 충전 후 사용해 주세요.";

    public PointNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
