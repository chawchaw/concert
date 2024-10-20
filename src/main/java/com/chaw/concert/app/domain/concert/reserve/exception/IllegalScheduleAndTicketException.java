package com.chaw.concert.app.domain.concert.reserve.exception;

public class IllegalScheduleAndTicketException extends IllegalArgumentException {

    public static final String DEFAULT_MESSAGE = "공연일정과 티켓이 일치하지 않습니다.";

    public IllegalScheduleAndTicketException() {
        super(DEFAULT_MESSAGE);
    }

}
