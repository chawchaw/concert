package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnterWaitQueueOutput (
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long order
){}
