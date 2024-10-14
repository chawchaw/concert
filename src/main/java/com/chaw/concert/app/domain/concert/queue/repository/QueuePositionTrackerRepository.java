package com.chaw.concert.app.domain.concert.queue.repository;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;

import java.util.List;

public interface QueuePositionTrackerRepository {
    QueuePositionTracker save(QueuePositionTracker queuePositionTracker);

    QueuePositionTracker findByConcertScheduleId(Long concertId);

    List<QueuePositionTracker> findAllByIsWaitQueueExist();
}
