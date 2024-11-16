package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.reserve.usecase.ReserveUseCase;
import lombok.Builder;

@Builder
public record RequestReserveOutput(
        Boolean success
){
    public static RequestReserveOutput of(ReserveUseCase.Output output) {
        return RequestReserveOutput.builder()
                .success(output.success())
                .build();
    }
}
