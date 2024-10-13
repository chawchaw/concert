package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueuePositionTrackerJpaRepository extends JpaRepository<QueuePositionTracker, Long> {
    Optional<QueuePositionTracker> findByConcertId(Long concertId);

    Boolean existsByConcertId(Long concertId);

    List<QueuePositionTracker> findAllByIsWaitQueueExist(Boolean isWaitQueueExist);
}
