package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PaidEvent;

public interface PaidEventRepository {
    void complete(PaidEvent event);
}
