package com.chaw.concert.app.domain.concert.query.exception;

public class TicketAlreadyReservedException extends IllegalStateException {

    public static final String DEFAULT_MESSAGE = "예약할 수 없는 티켓입니다.";

    public TicketAlreadyReservedException() {
        super(DEFAULT_MESSAGE);
    }

}
