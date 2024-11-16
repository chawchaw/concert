package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;

public interface ReserveEventRepository {
    void complete(ReserveEvent event);
}
