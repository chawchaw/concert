package com.chaw.concert.app.domain.concert.reserve.exception;

public class CanceledReserveException extends IllegalStateException {

    public static final String DEFAULT_MESSAGE = "취소된 예약입니다.";

    public CanceledReserveException() {
        super(DEFAULT_MESSAGE);
    }

}
