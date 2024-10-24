package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertScheduleRepositoryImpl implements ConcertScheduleRepository {
    private final ConcertScheduleJpaRepository repository;

    public ConcertScheduleRepositoryImpl(ConcertScheduleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ConcertSchedule> findByConcertIdAndIsSoldOut(Long concertId, boolean isSoldOut) {
        return repository.findByConcertIdAndIsSoldOut(concertId, isSoldOut);
    }

    @Override
    public ConcertSchedule save(ConcertSchedule concertSchedule) {
        return repository.save(concertSchedule);
    }

    @Override
    public boolean decreaseAvailableSeat(Long concertScheduleId) {
        return repository.decreaseAvailableSeat(concertScheduleId) > 0;
    }

    @Override
    public ConcertSchedule findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public ConcertSchedule findByIdWithLock(Long id) {
        return repository.findByIdWithLock(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
