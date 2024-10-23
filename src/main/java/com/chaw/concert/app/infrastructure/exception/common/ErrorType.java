package com.chaw.concert.app.infrastructure.exception.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

public enum ErrorType {
    BAD_REQUEST(IllegalArgumentException.class),                        // 400 - Bad Request
    UNAUTHORIZED(AuthenticationException.class),                        // 401 - Unauthorized
    FORBIDDEN(AccessDeniedException.class),                             // 403 - Forbidden
    NOT_FOUND(IllegalArgumentException.class),                          // 404 - Not Found
    CONFLICT(IllegalStateException.class),                              // 409 - Conflict
    DATA_INTEGRITY_VIOLATION(DataIntegrityViolationException.class),    // 500 - DB 관련 에러 (무결성 문제)
    SERVER_ERROR(RuntimeException.class);                               // 500 - Server Error

    private final Class<? extends RuntimeException> exceptionClass;

    ErrorType(Class<? extends RuntimeException> exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public Class<? extends RuntimeException> getExceptionClass() {
        return exceptionClass;
    }
}
