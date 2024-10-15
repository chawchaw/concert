package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;

import java.util.List;

public interface ConcertScheduleRepository {
    List<ConcertSchedule> findByConcertIdAndIsSold(Long concertId, boolean isSold);

    ConcertSchedule save(ConcertSchedule concertSchedule);
}
