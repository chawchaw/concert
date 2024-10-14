package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import org.springframework.stereotype.Repository;

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
    public Boolean existsByConcertScheduleIdAndUuid(Long concertScheduleId, String uuid) {
        return repository.existsByConcertScheduleIdAndUuid(concertScheduleId, uuid);
    }

    @Override
    public List<WaitQueue> findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(Long concertScheduleId, Long lastTransferredWaitingUserId) {
        return repository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(concertScheduleId, lastTransferredWaitingUserId);
    }

    @Override
    public void saveAll(List<WaitQueue> waitQueues) {
        repository.saveAll(waitQueues);
    }
}
