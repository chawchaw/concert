package com.chaw.concert.interfaces.api.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GetConcertsOutput {
    private Long id;
    private String name;
    private String status;
    private String info;
    private String artist;
    private String host;
    private String date;
    private String can_buy_from;
    private String can_buy_to;
    private String hall_name;
    private String hall_address;
    private String hall_address_detail;
    private String hall_location;
}
