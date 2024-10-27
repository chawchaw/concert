package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.query.usecase.GetConcertsUseCase;
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

    public static GetConcertsOutput of(GetConcertsUseCase.Output output) {
        return GetConcertsOutput.builder()
                .concerts(output.concerts().stream().map(concert -> Concert.builder()
                        .id(concert.id())
                        .name(concert.name())
                        .info(concert.info())
                        .artist(concert.artist())
                        .host(concert.host())
                        .build()).toList())
                .build();
    }
}
