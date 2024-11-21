package com.chaw.concert.app.domain.concert.reserve.usecase.dto;

public record PayEvent(
        Long concertScheduleId,
        Long ticketId,
        Long userId
){
    public String toMessage() {
        return String.format("결제 완료: concertScheduleId=%d, ticketId=%d, userId=%d", concertScheduleId, ticketId, userId);
    }
}
