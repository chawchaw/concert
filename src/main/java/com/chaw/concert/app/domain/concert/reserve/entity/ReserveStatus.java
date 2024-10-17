package com.chaw.concert.app.domain.concert.reserve.entity;

public enum ReserveStatus {
    RESERVE("예약"),
    PAID("결제완료"),
    CANCEL("취소");

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
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
}
