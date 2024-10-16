package com.chaw.concert.app.domain.concert.queue.entity;

public enum WaitQueueStatus {
    WAIT("진행중"),
    PASS("완료");

    private final String dbValue;

    WaitQueueStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static WaitQueueStatus fromDbValue(String dbValue) {
        for (WaitQueueStatus type : WaitQueueStatus.values()) {
            if (type.dbValue.equals(dbValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
}
