package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository repository;

    public ConcertRepositoryImpl(ConcertJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Concert save(Concert concert) {
        return repository.save(concert);
    }

    @Override
    public List<Concert> findAll() {
        return repository.findAll();
    }
}
