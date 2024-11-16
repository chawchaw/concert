package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;
import com.chaw.concert.app.domain.concert.queue.usecase.GetUserNodeUseCase;
import lombok.Builder;

@Builder
public record UserNodeOutput(
        UserNodeStatus status,
        Integer order
){
    public static UserNodeOutput of(GetUserNodeUseCase.Output output) {
        return UserNodeOutput.builder()
                .status(output.status())
                .order(output.order())
                .build();
    }
}
