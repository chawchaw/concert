package com.chaw.concert.app.presenter.controller.api.v1.concert.dto;

import java.time.LocalDateTime;

public record GetConcertSchedulesInput(
        LocalDateTime dateConcertFrom,
        LocalDateTime dateConcertTo
) {}
