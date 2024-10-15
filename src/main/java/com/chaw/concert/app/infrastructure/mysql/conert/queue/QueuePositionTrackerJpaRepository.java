package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueuePositionTrackerJpaRepository extends JpaRepository<QueuePositionTracker, Long> {
    Optional<QueuePositionTracker> findByConcertScheduleId(Long concertScheduleId);

    List<QueuePositionTracker> findAllByIsWaitQueueExist(Boolean isWaitQueueExist);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueuePositionTracker q WHERE q.concertScheduleId = :concertScheduleId")
    Optional<QueuePositionTracker> findByConcertScheduleIdWithLock(Long concertScheduleId);
}
