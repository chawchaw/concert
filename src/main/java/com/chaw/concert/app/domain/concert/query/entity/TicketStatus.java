package com.chaw.concert.app.domain.concert.query.entity;

public enum TicketStatus {
    EMPTY("공석"),
    TEMP_RESERVATION("임시예약"),
    COMPLETED_RESERVATION("예약완료");

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
