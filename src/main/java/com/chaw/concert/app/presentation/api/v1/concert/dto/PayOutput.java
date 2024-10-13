package com.chaw.concert.app.presentation.api.v1.concert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PayOutput {
    private Long id;
    private String status;
    private LocalDateTime booked_at;
    private Integer point_used;
    private Integer point_balance;
}
