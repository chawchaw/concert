package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QueuePositionTrackerRepositoryImpl implements QueuePositionTrackerRepository {

    private final QueuePositionTrackerJpaRepository repository;

    public QueuePositionTrackerRepositoryImpl(QueuePositionTrackerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public QueuePositionTracker save(QueuePositionTracker queuePositionTracker) {
        return repository.save(queuePositionTracker);
    }

    @Override
    public QueuePositionTracker findByConcertScheduleId(Long concertScheduleId) {
        return repository.findByConcertScheduleId(concertScheduleId).orElse(null);
    }

    @Override
    public List<QueuePositionTracker> findAllByIsWaitQueueExist() {
        return repository.findAllByIsWaitQueueExist(true);
    }

    @Override
    public Optional<QueuePositionTracker> findByConcertScheduleIdWithLock(Long concertScheduleId) {
        return repository.findByConcertScheduleIdWithLock(concertScheduleId);
    }
}
