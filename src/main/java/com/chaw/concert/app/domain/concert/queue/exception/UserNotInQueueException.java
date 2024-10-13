package com.chaw.concert.app.domain.concert.queue.exception;

public class UserNotInQueueException extends RuntimeException {

    public static final String DEFAULT_MESSAGE = "대기열에 존재하지 않는 유저입니다.";

    public UserNotInQueueException() {
        super(DEFAULT_MESSAGE);
    }

}
