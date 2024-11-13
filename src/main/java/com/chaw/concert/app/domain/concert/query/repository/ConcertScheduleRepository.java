package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertScheduleRepository {

    List<ConcertSchedule> findByConcertIdAndIsSoldOutAndDateConcertBetween(Long concertId, boolean isSoldOut, LocalDateTime dateConcertFrom, LocalDateTime dateConcertTo);

    ConcertSchedule findByIdOrThrow(Long id);

    ConcertSchedule save(ConcertSchedule concertSchedule);

    boolean decreaseAvailableSeat(Long concertScheduleId);

}
