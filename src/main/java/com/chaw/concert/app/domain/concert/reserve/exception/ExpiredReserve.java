package com.chaw.concert.app.domain.concert.reserve.exception;

public class ExpiredReserve extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "결제 유효기간이 만료되었습니다.";

    public ExpiredReserve() {
        super(DEFAULT_MESSAGE);
    }

}
