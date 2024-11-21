package com.chaw.concert.app.domain.concert.reserve.usecase.dto;

import lombok.Builder;

@Builder
public record ReservedEvent(
        Long concertScheduleId,
        Long ticketId,
        Long userId,
        Long concertOutboxId
){
    public String toMessage() {
        return String.format("예약 완료: concertScheduleId=%d, ticketId=%d, userId=%d, concertOutboxId=%d", concertScheduleId, ticketId, userId, concertOutboxId);
    }
}
