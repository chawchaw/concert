package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import jakarta.validation.constraints.Min;

public record ChargePointInput (
        @Min(1)
        Integer point
) {}
