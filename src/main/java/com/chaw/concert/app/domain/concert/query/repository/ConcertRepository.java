package com.chaw.concert.app.domain.concert.query.repository;

import com.chaw.concert.app.domain.concert.query.entity.Concert;

public interface ConcertRepository {
    Concert save(Concert concert);
}
