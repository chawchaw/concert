package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import com.chaw.concert.app.domain.common.user.usecase.ChargePointUseCase;
import lombok.Builder;

@Builder
public record ChargePointOutput(
        Integer balance
) {
    public static ChargePointOutput of(ChargePointUseCase.Output output) {
        return ChargePointOutput.builder()
                .balance(output.balance())
                .build();
    }
}
