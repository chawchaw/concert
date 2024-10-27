package com.chaw.concert.app.domain.concert.queue.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;

public enum WaitQueueStatus {
    WAIT("WAIT"), // 대기
    PASS("PASS"); // 통과

    private final String dbValue;

    WaitQueueStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static WaitQueueStatus fromDbValue(String dbValue) {
        for (WaitQueueStatus type : WaitQueueStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "Invalid WaitQueueStatus value: " + dbValue);
    }
}
