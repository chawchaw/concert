package com.chaw.concert.app.domain.concert.reserve.exception;

public class TicketNotInStatusReserveException extends IllegalStateException {

    public static final String DEFAULT_MESSAGE = "예약된 티켓이 아닙니다.";

    public TicketNotInStatusReserveException() {
        super(DEFAULT_MESSAGE);
    }

}
