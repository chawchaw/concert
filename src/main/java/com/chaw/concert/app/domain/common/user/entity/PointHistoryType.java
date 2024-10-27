package com.chaw.concert.app.domain.common.user.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;

public enum PointHistoryType {
    CHARGE("CHARGE"), // 충전
    PAY("PAY"); // 결제

    private final String dbValue;

    PointHistoryType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static PointHistoryType fromDbValue(String dbValue) {
        for (PointHistoryType type : PointHistoryType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "Invalid PointHistoryType value: " + dbValue);
    }
}
