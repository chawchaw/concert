package com.chaw.concert.app.domain.common.user.entity;

public enum PointHistoryType {
    CHARGE("충전"),
    PAY("결제");

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
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
}
