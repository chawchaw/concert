package com.chaw.concert.app.domain.concert.reserve.entity;

public enum PaymentMethod {
    POINT("포인트"),
    CARD("카드"),
    ACCOUNT_TRANSFER("계좌이체");

    private final String dbValue;

    PaymentMethod(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static PaymentMethod fromDbValue(String dbValue) {
        for (PaymentMethod type : PaymentMethod.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
}
