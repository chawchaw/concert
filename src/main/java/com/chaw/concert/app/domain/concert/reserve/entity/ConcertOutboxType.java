package com.chaw.concert.app.domain.concert.reserve.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;

public enum ConcertOutboxType {
    RESERVED("RESERVED"),
    PAID("PAID");

    private final String dbValue;

    ConcertOutboxType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ConcertOutboxType fromDbValue(String dbValue) {
        for (ConcertOutboxType type : ConcertOutboxType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "Invalid ConcertOutboxType value: " + dbValue);
    }
}
