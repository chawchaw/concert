package com.chaw.concert.app.domain.concert.queue.repository;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;

import java.util.List;

public interface WaitQueueRepository {
    WaitQueue save(WaitQueue waitQueue);

    WaitQueue findByUserId(Long userId);

    Long countByStatusAndIdLessThan(WaitQueueStatus waitQueueStatus, Long id);

    int countByStatus(WaitQueueStatus status);

    void deleteAll();

    List<WaitQueue> findByStatusByLimit(WaitQueueStatus waitQueueStatus, Integer passSize);
}
