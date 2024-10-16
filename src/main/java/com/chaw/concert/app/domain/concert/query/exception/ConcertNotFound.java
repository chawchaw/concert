package com.chaw.concert.app.domain.concert.query.exception;

public class ConcertNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 콘서트입니다.";

    public ConcertNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
