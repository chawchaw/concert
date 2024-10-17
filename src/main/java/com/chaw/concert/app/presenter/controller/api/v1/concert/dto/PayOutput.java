package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import java.time.LocalDateTime;

public record PayOutput (
    Long id,
    String status,
    LocalDateTime booked_at,
    Integer point_used,
    Integer point_balance
) {}
