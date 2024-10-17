package com.chaw.concert.app.domain.concert.query.exception;

import jakarta.persistence.EntityNotFoundException;

public class ConcertScheduleNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 콘서트 일정입니다.";

    public ConcertScheduleNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
