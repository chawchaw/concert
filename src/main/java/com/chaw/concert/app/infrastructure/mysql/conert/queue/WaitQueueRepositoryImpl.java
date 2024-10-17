package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class WaitQueueRepositoryImpl implements WaitQueueRepository {

    private final WaitQueueJpaRepository repository;

    public WaitQueueRepositoryImpl(WaitQueueJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public WaitQueue save(WaitQueue waitQueue) {
        return repository.save(waitQueue);
    }

    @Override
    public WaitQueue findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Long countByStatusAndIdLessThan(WaitQueueStatus waitQueueStatus, Long id) {
        return repository.countByStatusAndIdLessThan(waitQueueStatus, id);
    }

    @Override
    public int countByStatus(WaitQueueStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public List<WaitQueue> findByStatusByLimit(WaitQueueStatus waitQueueStatus, Integer passSize) {
        return repository.findByStatusByLimit(waitQueueStatus, passSize);
    }

    @Override
    public List<WaitQueue> findByStatusAndUpdatedAtBefore(WaitQueueStatus status, LocalDateTime expiredAt) {
        return repository.findByStatusAndUpdatedAtBefore(status, expiredAt);
    }

    @Override
    public void delete(WaitQueue waitQueue) {
        repository.delete(waitQueue);
    }

}
