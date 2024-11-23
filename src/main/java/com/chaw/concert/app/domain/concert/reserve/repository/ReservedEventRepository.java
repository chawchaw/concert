package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;

public interface ReservedEventRepository {
    void complete(ReservedEvent event);
}
