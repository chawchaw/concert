package com.chaw.concert.app.domain.concert.reserve.exception;

public class CanceledReserve extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "취소된 예약입니다.";

    public CanceledReserve() {
        super(DEFAULT_MESSAGE);
    }

}
