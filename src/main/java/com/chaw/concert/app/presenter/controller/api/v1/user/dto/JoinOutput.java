package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import com.chaw.concert.app.domain.common.auth.usecase.JoinUseCase;
import lombok.Builder;

@Builder
public record JoinOutput (
        Boolean result,
        String username
) {
    public static JoinOutput of(JoinUseCase.Output output) {
        return JoinOutput.builder()
                .result(output.result())
                .username(output.username())
                .build();
    }
}
