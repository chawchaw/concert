package com.chaw.concert.app.infrastructure.mysql.conert.query;

import com.chaw.concert.app.domain.concert.query.repository.HallRepository;
import org.springframework.stereotype.Repository;

@Repository
public class HallRepositoryImpl implements HallRepository {
    private final HallJpaRepository repository;

    public HallRepositoryImpl(HallJpaRepository repository) {
        this.repository = repository;
    }
}
