package com.chaw.concert.app.domain.concert.query.entity;

public enum TicketStatus {
    EMPTY("EMPTY"), // 공석
    RESERVE("RESERVE"), // 예약
    PAID("PAID"); // 결제완료

    private final String dbValue;

    TicketStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TicketStatus fromDbValue(String dbValue) {
        for (TicketStatus type : TicketStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
}
