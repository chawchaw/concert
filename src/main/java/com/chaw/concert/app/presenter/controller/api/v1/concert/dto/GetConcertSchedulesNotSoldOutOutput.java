package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetConcertSchedulesNotSoldOutOutput (
        Long id,
        String name,
        String info,
        String artist,
        String host,
        List<Item> schedules
) {
    @Builder
    public record Item (
            Long id,
            Boolean isSoldOut,
            Integer totalSeat,
            Integer availableSeat,
            LocalDateTime dateConcert
    ) {}
}
