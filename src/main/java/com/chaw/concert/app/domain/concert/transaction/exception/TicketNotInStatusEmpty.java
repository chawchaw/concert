package com.chaw.concert.app.domain.concert.transaction.exception;

public class TicketNotInStatusEmpty extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "예약된 티켓이 아닙니다.";

    public TicketNotInStatusEmpty() {
        super(DEFAULT_MESSAGE);
    }

}
