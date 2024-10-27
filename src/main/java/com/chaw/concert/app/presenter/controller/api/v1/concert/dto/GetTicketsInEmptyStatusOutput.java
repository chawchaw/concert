package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatusUseCase;
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

    public static GetTicketsInEmptyStatusOutput of(GetTicketsInEmptyStatusUseCase.Output output) {
        return GetTicketsInEmptyStatusOutput.builder()
                .concertScheduleId(output.concertScheduleId())
                .tickets(output.tickets().stream().map(ticket -> GetTicketsInEmptyStatusOutput.Item.builder()
                        .id(ticket.id())
                        .type(ticket.type())
                        .seatNo(ticket.seatNo())
                        .price(ticket.price())
                        .build()).toList())
                .build();
    }
}
