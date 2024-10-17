package com.chaw.concert.app.domain.concert.queue.exception;

public class WaitQueueNotFound extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "대기열이 존재하지 않습니다.";

    public WaitQueueNotFound() {
        super(DEFAULT_MESSAGE);
    }

}
