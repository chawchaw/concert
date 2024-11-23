package com.chaw.concert.app.domain.concert.reserve.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;

public enum ConcertOutboxStatus {
    INIT("INIT"),
    RETRY("RETRY"),
    FAILED("FAILED"),
    PUBLISHED("PUBLISHED");

    private final String dbValue;

    ConcertOutboxStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ConcertOutboxStatus fromDbValue(String dbValue) {
        for (ConcertOutboxStatus type : ConcertOutboxStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "Invalid ConcertOutboxStatus value: " + dbValue);
    }
}
