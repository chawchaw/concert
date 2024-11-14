package com.chaw.concert.app.domain.concert.reserve.usecase.dto;

public record PayEvent(
        Long concertScheduleId,
        Long ticketId,
        Long userId
){}
