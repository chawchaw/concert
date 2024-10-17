package com.chaw.concert.app.domain.concert.reserve.exception;

public class ReserveNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "예약 내역이 없습니다.";

    public ReserveNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
