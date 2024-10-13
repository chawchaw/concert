package com.chaw.concert.app.domain.concert.queue.exception;

public class WaitQueueIndicatorNotExist extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "대기열 추척이 시작되지 않은 콘서트입니다.";

    public WaitQueueIndicatorNotExist() {
        super(DEFAULT_MESSAGE);
    }

}
