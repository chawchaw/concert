package com.chaw.concert.app.domain.common.user.exception;

public class NotEnoughBalance extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "잔액이 부족합니다.";

    public NotEnoughBalance() {
        super(DEFAULT_MESSAGE);
    }

}
