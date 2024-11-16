package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.query.usecase.GetConcertSchedulesUseCase;
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

    public static GetConcertSchedulesNotSoldOutOutput of(GetConcertSchedulesUseCase.Output output) {
        return GetConcertSchedulesNotSoldOutOutput.builder()
                .id(output.id())
                .name(output.name())
                .info(output.info())
                .artist(output.artist())
                .host(output.host())
                .schedules(output.schedules().stream().map(schedule -> Item.builder()
                        .id(schedule.id())
                        .isSoldOut(schedule.isSoldOut())
                        .totalSeat(schedule.totalSeat())
                        .availableSeat(schedule.availableSeat())
                        .dateConcert(schedule.dateConcert())
                        .build()
                ).toList())
                .build();
    }
}
