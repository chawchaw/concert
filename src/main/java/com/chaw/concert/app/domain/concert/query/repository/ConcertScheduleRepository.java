package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;

import java.util.List;

public interface ConcertScheduleRepository {
    List<ConcertSchedule> findByConcertIdAndIsSoldOut(Long concertId, boolean isSoldOut);

    ConcertSchedule findByIdOrThrow(Long id);

    ConcertSchedule findByIdWithLockThrow(Long id);

    ConcertSchedule save(ConcertSchedule concertSchedule);

    boolean decreaseAvailableSeat(Long concertScheduleId);

    void deleteAll();
}
