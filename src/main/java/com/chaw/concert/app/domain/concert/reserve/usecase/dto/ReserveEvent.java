package com.chaw.concert.app.domain.concert.reserve.usecase.dto;

public record ReserveEvent (
        Long concertScheduleId,
        Long ticketId,
        Long userId
){}
