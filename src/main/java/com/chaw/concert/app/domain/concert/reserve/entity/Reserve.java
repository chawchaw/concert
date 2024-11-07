package com.chaw.concert.app.domain.concert.reserve.entity;

public record Reserve (Long concertScheduleId, Long ticketId, Long userId) {
    public final static Integer RESERVE_LIMIT_SECONDS = 5;
}
