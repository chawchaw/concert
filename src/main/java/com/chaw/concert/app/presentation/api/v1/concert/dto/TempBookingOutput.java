package com.chaw.concert.app.presentation.api.v1.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class TempBookingOutput {
    private Long id;
    private String status;
    private LocalDateTime temp_booking_end_at;
}
