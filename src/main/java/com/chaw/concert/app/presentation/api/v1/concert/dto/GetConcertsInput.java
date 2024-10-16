package com.chaw.concert.app.presentation.api.v1.concert.dto;

import java.time.LocalDateTime;

public record GetConcertsInput (
    LocalDateTime from,
    LocalDateTime to
) {}
