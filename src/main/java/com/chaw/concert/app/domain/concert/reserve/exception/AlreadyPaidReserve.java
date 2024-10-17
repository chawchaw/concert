package com.chaw.concert.app.domain.concert.reserve.exception;

public class AlreadyPaidReserve extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "결제 완료된 예약입니다.";

    public AlreadyPaidReserve() {
        super(DEFAULT_MESSAGE);
    }

}
