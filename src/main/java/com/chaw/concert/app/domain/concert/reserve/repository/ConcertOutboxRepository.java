package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;

public interface ConcertOutboxRepository {
    void save(ConcertOutbox concertOutbox);

    ConcertOutbox findByIdOrThrow(Long id);

    ConcertOutbox findByIdAndTypeOrThrow(Long id, ConcertOutboxType concertOutboxType);
}
