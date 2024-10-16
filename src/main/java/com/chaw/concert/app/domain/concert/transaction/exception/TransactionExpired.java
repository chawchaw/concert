package com.chaw.concert.app.domain.concert.transaction.exception;

public class TransactionExpired extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "결제 유효기간이 만료되었습니다.";

    public TransactionExpired() {
        super(DEFAULT_MESSAGE);
    }

}
