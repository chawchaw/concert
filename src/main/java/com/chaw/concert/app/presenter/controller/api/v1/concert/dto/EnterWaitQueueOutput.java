package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitTokenUseCase;
import lombok.Builder;

@Builder
public record EnterWaitQueueOutput (
        String status,
        Integer order
){
    public static EnterWaitQueueOutput of(EnterWaitTokenUseCase.Output output) {
        return EnterWaitQueueOutput.builder()
                .status(output.status())
                .order(output.order())
                .build();
    }
}
