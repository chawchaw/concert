package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PayEvent;

public interface PayEventRepository {
    void complete(PayEvent event);
}
