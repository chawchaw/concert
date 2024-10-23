package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
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
        Concert concert = repository.findById(id).orElse(null);
        throwNotFoundException(concert);
        return concert;
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    private void throwNotFoundException(Concert concert) {
        if (concert == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "없는 콘서트입니다.");
        }
    }
}
