package com.chaw.concert.interfaces.api.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class GetTicketsOutput {
    private List<Item> items;

    @Data
    @AllArgsConstructor
    @Builder
    public static class Item {
        private Long id;
        private String status;
        private String seat_zone;
        private String seat_no;
        private String seat_type;
        private Integer seat_price;
    }
}
