package com.chaw.concert.app.domain.concert.reserve.repository;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertOutboxRepository {
    void save(ConcertOutbox concertOutbox);

    ConcertOutbox findByIdOrThrow(Long id);

    ConcertOutbox findByIdAndTypeOrThrow(Long id, ConcertOutboxType concertOutboxType);

    List<ConcertOutbox> findAllByStatusInAndCreatedAtBefore(List<ConcertOutboxStatus> statuses, LocalDateTime before);

    List<ConcertOutbox> findAll();
}
