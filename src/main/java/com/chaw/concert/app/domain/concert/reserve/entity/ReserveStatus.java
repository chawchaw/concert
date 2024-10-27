package com.chaw.concert.app.domain.concert.reserve.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;

public enum ReserveStatus {
    RESERVE("RESERVE"), // 예약
    PAID("PAID"), // 결제완료
    CANCEL("CANCEL"); // 취소

    private final String dbValue;

    ReserveStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static ReserveStatus fromDbValue(String dbValue) {
        for (ReserveStatus type : ReserveStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "Invalid ReserveStatus value: " + dbValue);
    }
}
