package com.chaw.concert.app.domain.concert.queue.exception;

import jakarta.persistence.EntityNotFoundException;

public class WaitQueueNotFoundException extends EntityNotFoundException {

    public static final String DEFAULT_MESSAGE = "대기열이 존재하지 않습니다.";

    public WaitQueueNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

}
