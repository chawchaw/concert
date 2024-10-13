package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReservationPhaseRepositoryImpl implements ReservationPhaseRepository {

    private final ReservationPhaseJpaRepository repository;

    public ReservationPhaseRepositoryImpl(ReservationPhaseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ReservationPhase save(ReservationPhase reservationPhase) {
        return repository.save(reservationPhase);
    }

    @Override
    public Integer countByConcertId(Long concertId) {
        return repository.countByConcertId(concertId);
    }

    @Override
    public void saveAll(List<ReservationPhase> reservationPhases) {
        repository.saveAll(reservationPhases);
    }

    @Override
    public void deleteByUuid(String uuid) {
        repository.deleteByUuid(uuid);
    }

    @Override
    public Optional<ReservationPhase> findByUuid(String uuid) {
        return repository.findByUuid(uuid);
    }

    @Override
    public Optional<ReservationPhase> findByConcertIdAndUuid(Long concertId, String uuid) {
        return repository.findByConcertIdAndUuid(concertId, uuid);
    }
}
