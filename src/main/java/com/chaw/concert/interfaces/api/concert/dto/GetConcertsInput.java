package com.chaw.concert.interfaces.api.concert.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class GetConcertsInput {
    private LocalDateTime from;
    private LocalDateTime to;
}
