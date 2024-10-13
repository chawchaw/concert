package com.chaw.concert.app.presentation.api.v1.concert.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class GetConcertsInput {
    private LocalDateTime from;
    private LocalDateTime to;
}
