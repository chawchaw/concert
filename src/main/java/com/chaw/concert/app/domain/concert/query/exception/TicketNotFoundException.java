package com.chaw.concert.app.domain.concert.query.exception;

import jakarta.persistence.EntityNotFoundException;

public class TicketNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 티켓입니다.";

    public TicketNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
