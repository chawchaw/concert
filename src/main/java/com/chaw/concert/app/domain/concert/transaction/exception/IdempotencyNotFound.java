package com.chaw.concert.app.domain.concert.transaction.exception;

public class IdempotencyNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "유효하지 않는 멱등성 키입니다.";

    public IdempotencyNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
