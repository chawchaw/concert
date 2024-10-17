package com.chaw.concert.app.domain.concert.query.exception;

import jakarta.persistence.EntityNotFoundException;

public class ConcertNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 콘서트입니다.";

    public ConcertNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
