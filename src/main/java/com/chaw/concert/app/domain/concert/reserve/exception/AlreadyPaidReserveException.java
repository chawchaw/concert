package com.chaw.concert.app.domain.concert.reserve.exception;

public class AlreadyPaidReserveException extends IllegalStateException {

    public static final String DEFAULT_MESSAGE = "결제 완료된 예약입니다.";

    public AlreadyPaidReserveException() {
        super(DEFAULT_MESSAGE);
    }

}
