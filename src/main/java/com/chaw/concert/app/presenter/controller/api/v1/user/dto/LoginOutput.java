package com.chaw.concert.app.presenter.controller.api.v1.user.dto;

import lombok.Builder;

@Builder
public record LoginOutput (
        String token
) {}
