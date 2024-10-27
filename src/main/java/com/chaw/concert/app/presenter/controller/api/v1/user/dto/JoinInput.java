package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import jakarta.validation.constraints.NotNull;

public record JoinInput(
        @NotNull
        String username,
        @NotNull
        String password
) {}
