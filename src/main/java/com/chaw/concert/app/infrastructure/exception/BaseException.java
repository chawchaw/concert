package com.chaw.concert.app.infrastructure.exception;

public class BaseException extends RuntimeException {
    private final ErrorType errorType;

    public BaseException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    // 예외 유형에 맞는 자바 기본 예외로 변환하는 메서드
    public RuntimeException toStandardException() {
        try {
            return errorType.getExceptionClass()
                    .getConstructor(String.class)
                    .newInstance(getMessage());
        } catch (Exception e) {
            throw new RuntimeException("처리되지 않은 에러", e);
        }
    }
}
