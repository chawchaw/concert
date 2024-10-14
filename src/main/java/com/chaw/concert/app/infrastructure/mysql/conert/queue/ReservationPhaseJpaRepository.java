package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationPhaseJpaRepository extends JpaRepository<ReservationPhase, Long> {
    Integer countByConcertScheduleId(Long concertScheduleId);

    void deleteByUuid(String uuid);

    Optional<ReservationPhase> findByUuid(String uuid);

    Optional<ReservationPhase> findByConcertScheduleIdAndUuid(Long concertScheduleId, String uuid);
}
