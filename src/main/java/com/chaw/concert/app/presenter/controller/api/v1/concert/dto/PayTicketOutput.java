package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketUseCase;
import lombok.Builder;

@Builder
public record PayTicketOutput(
        Boolean success,
        Long paymentId,
        Integer balance
){
    public static PayTicketOutput of(PayTicketUseCase.Output output) {
        return PayTicketOutput.builder()
                .success(output.success())
                .paymentId(output.paymentId())
                .balance(output.balance())
                .build();
    }
}
