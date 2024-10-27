package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GetTicketsInEmptyStatusOutput (
        Long concertScheduleId,
        List<Item> tickets
) {
    @Builder
    public record Item (
            Long id,
            String type,
            String seatNo,
            Integer price
    ) {}
}
