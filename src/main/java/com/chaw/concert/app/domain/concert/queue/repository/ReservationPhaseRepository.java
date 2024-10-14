package com.chaw.concert.app.domain.concert.queue.repository;

import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;

import java.util.List;
import java.util.Optional;

public interface ReservationPhaseRepository {
    ReservationPhase save(ReservationPhase reservationPhase);

    Integer countByConcertScheduleId(Long concertId);

    void saveAll(List<ReservationPhase> reservationPhases);

    void deleteByUuid(String uuid);

    Optional<ReservationPhase> findByUuid(String uuid);

    Optional<ReservationPhase> findByConcertScheduleIdAndUuid(Long concertId, String uuid);
}
