package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public QueuePositionTracker findByConcertId(Long concertId) {
        return repository.findByConcertId(concertId).orElse(null);
    }

    @Override
    public List<QueuePositionTracker> findAllByIsWaitQueueExist() {
        return repository.findAllByIsWaitQueueExist(true);
    }
}
