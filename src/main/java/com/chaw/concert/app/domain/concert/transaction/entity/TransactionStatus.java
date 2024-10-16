package com.chaw.concert.app.domain.concert.transaction.entity;

public enum TransactionStatus {
    PENDING("진행중"),
    COMPLETED("완료"),
    FAILED("실패"),
    EXPIRED("만료");

    private final String dbValue;

    TransactionStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TransactionStatus fromDbValue(String dbValue) {
        for (TransactionStatus type : TransactionStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
}
