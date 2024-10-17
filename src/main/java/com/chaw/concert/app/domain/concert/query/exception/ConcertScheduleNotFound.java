package com.chaw.concert.app.domain.concert.query.exception;

public class ConcertScheduleNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 콘서트 일정입니다.";

    public ConcertScheduleNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
