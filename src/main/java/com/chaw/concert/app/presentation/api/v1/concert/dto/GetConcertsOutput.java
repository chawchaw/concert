package com.chaw.concert.app.presentation.api.v1.concert.dto;

public record GetConcertsOutput (
    Long id,
    String name,
    String status,
    String info,
    String artist,
    String host,
    String date,
    String can_buy_from,
    String can_buy_to,
    String hall_name,
    String hall_address,
    String hall_address_detail,
    String hall_location
) {}
