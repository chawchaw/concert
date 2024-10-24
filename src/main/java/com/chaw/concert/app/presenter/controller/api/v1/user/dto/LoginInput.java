package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import jakarta.validation.constraints.NotNull;

public record LoginInput(
        @NotNull
        String username,
        @NotNull
        String password
) {}
