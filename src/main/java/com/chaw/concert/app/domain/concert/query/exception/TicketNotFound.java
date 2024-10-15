package com.chaw.concert.app.domain.concert.query.exception;

public class TicketNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 티켓입니다.";

    public TicketNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
