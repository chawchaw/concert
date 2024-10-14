package com.chaw.concert.app.domain.concert.queue.repository;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;

import java.util.List;

public interface WaitQueueRepository {
    WaitQueue save(WaitQueue waitQueue);

    Boolean existsByConcertScheduleIdAndUuid(Long concertId, String uuid);

    List<WaitQueue> findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(Long concertId, Long waitingUserId);

    void saveAll(List<WaitQueue> waitQueues);
}
