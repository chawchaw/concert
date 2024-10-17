package com.chaw.concert.app.domain.concert.reserve.exception;

public class ReserveExpired extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "결제 유효기간이 만료되었습니다.";

    public ReserveExpired() {
        super(DEFAULT_MESSAGE);
    }

}
