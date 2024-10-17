package com.chaw.concert.app.domain.concert.reserve.exception;

import jakarta.persistence.EntityNotFoundException;

public class ReserveNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "예약 내역이 없습니다.";

    public ReserveNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
