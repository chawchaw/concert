package com.chaw.concert.app.domain.concert.query.entity;

import com.chaw.concert.app.infrastructure.exception.BaseException;
import com.chaw.concert.app.infrastructure.exception.ErrorType;

public enum TicketType {
    VIP("VIP"), // VIP
    FIRST("FIRST"), // 1등석
    SECOND("SECOND"); // 2등석

    private final String dbValue;

    TicketType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TicketType fromDbValue(String dbValue) {
        for (TicketType type : TicketType.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "Invalid TicketType value: " + dbValue);
    }
}
