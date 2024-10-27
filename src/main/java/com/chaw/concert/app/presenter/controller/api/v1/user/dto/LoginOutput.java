package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import com.chaw.concert.app.domain.common.auth.usecase.LoginUseCase;
import lombok.Builder;

@Builder
public record LoginOutput (
        String token
) {
    public static LoginOutput of(LoginUseCase.Output output) {
        return LoginOutput.builder()
                .token(output.token())
                .build();
    }
}
