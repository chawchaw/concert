package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
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
        ConcertSchedule concertSchedule = repository.findById(id).orElse(null);
        throwNotFoundException(concertSchedule);
        return concertSchedule;
    }

    @Override
    public ConcertSchedule findByIdWithLock(Long id) {
        ConcertSchedule concertSchedule = repository.findByIdWithLock(id);
        throwNotFoundException(concertSchedule);
        return concertSchedule;
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    private void throwNotFoundException(ConcertSchedule concertSchedule) {
        if (concertSchedule == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "없는 일정입니다.");
        }
    }
}
