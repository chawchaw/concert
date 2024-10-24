package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GetConcertsOutput(
        List<Concert> concerts
){
    @Builder
    public record Concert(
            Long id,
            String name,
            String info,
            String artist,
            String host
    ){}
}
