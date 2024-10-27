package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import com.chaw.concert.app.domain.common.user.usecase.GetPointUseCase;
import lombok.Builder;

@Builder
public record GetPointOutput (
        Integer point
){
    public static GetPointOutput of(GetPointUseCase.Output output) {
        return GetPointOutput.builder()
                .point(output.point())
                .build();
    }
}
