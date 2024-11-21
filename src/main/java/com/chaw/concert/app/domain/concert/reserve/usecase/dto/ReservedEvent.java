package com.chaw.concert.app.domain.concert.reserve.usecase.dto;

public record ReservedEvent(
        Long concertScheduleId,
        Long ticketId,
        Long userId
){
    public String toMessage() {
        return String.format("예약 완료: concertScheduleId=%d, ticketId=%d, userId=%d", concertScheduleId, ticketId, userId);
    }
}
