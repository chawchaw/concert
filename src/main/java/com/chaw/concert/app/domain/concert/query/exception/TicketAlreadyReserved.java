package com.chaw.concert.app.domain.concert.query.exception;

public class TicketAlreadyReserved extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "예약할 수 없는 티켓입니다.";

    public TicketAlreadyReserved() {
        super(DEFAULT_MESSAGE);
    }

}
