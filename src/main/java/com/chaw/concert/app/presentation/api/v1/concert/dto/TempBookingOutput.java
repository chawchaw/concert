package com.chaw.concert.app.presentation.api.v1.concert.dto;

import java.time.LocalDateTime;

public record TempBookingOutput (
    Long id,
    String status,
    LocalDateTime temp_booking_end_at
) {}
