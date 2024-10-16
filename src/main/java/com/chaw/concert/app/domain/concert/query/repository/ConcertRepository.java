package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.Concert;

import java.util.List;

public interface ConcertRepository {
    Concert save(Concert concert);

    List<Concert> findAll();

    Boolean existsById(Long concertId);
}
