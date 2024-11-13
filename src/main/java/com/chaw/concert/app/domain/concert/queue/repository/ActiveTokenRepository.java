package com.chaw.concert.app.domain.concert.queue.repository;

public interface ActiveTokenRepository {

    Boolean existsByUserId(Long userId); // 활성 토큰 조회

    void save(Long userId, Integer timeToLiveSeconds);

    long countAll();
}
