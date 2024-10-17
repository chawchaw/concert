package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import java.util.List;

public record GetTicketsOutput (
    List<Item> items
) {
    public record Item (
            Long id,
            String status,
            String seat_zone,
            String seat_no,
            String seat_type,
            Integer seat_price
    ) {}
}
