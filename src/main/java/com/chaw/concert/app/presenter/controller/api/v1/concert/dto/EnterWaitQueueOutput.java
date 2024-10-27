package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueueUseCase;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnterWaitQueueOutput (
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long order
){
    public static EnterWaitQueueOutput of(EnterWaitQueueUseCase.Output output) {
        return EnterWaitQueueOutput.builder()
                .status(output.status())
                .createdAt(output.createdAt())
                .updatedAt(output.updatedAt())
                .order(output.order())
                .build();
    }
}
