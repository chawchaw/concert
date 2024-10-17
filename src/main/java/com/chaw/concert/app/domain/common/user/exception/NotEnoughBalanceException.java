package com.chaw.concert.app.domain.common.user.exception;

public class NotEnoughBalanceException extends IllegalStateException {

    public static final String DEFAULT_MESSAGE = "잔액이 부족합니다.";

    public NotEnoughBalanceException() {
        super(DEFAULT_MESSAGE);
    }

}
