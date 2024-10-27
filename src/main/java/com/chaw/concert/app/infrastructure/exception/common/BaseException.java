package com.chaw.concert.app.infrastructure.exception.common;

public class BaseException extends RuntimeException {
    private final ErrorType errorType;

    public BaseException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
