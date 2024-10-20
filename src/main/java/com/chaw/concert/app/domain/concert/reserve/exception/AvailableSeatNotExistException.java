package com.chaw.concert.app.domain.concert.reserve.exception;

public class AvailableSeatNotExistException extends IllegalArgumentException {

    public static final String DEFAULT_MESSAGE = "남은 좌석이 없습니다.";

    public AvailableSeatNotExistException() {
        super(DEFAULT_MESSAGE);
    }

}
