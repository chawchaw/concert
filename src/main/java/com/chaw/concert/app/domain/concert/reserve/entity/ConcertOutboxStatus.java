package com.chaw.concert.app.domain.concert.reserve.entity;

public enum ConcertOutboxStatus {
    INIT,
    RETRY,
    FAILED,
    PUBLISHED;
}
