package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.infrastructure.exception.BaseException;
import com.chaw.concert.app.infrastructure.exception.ErrorType;
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

    @Override
    public Boolean existsById(Long concertId) {
        return repository.existsById(concertId);
    }

    @Override
    public Concert findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BaseException(ErrorType.NOT_FOUND, "Concert not found"));
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
