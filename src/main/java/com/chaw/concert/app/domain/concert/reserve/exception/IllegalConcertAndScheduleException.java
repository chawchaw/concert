package com.chaw.concert.app.domain.concert.reserve.exception;

public class IllegalConcertAndScheduleException extends IllegalArgumentException {

    public static final String DEFAULT_MESSAGE = "콘서트와 공연일정이 일치하지 않습니다.";

    public IllegalConcertAndScheduleException() {
        super(DEFAULT_MESSAGE);
    }

}
