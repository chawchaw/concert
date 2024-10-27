package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import lombok.Builder;

@Builder
public record PayTicketOutput(
        Boolean success,
        Long paymentId,
        Integer balance
){}
