package com.chaw.concert.interfaces.api.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GetTicketsOutput {
    private Long id;
    private String status;
    private String seat_zone;
    private String seat_no;
    private String seat_type;
    private Integer seat_price;
}
