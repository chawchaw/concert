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
    public List<ConcertSchedule> findByConcertIdAndIsSold(Long concertId, boolean isSold) {
        return repository.findByConcertIdAndIsSold(concertId, isSold);
    }
}