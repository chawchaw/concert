package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaitQueueJpaRepository extends JpaRepository<WaitQueue, Long> {

    WaitQueue findByUserId(Long userId);

    Long countByStatusAndIdLessThan(WaitQueueStatus waitQueueStatus, Long id);

    int countByStatus(WaitQueueStatus status);

    @Query("SELECT w FROM WaitQueue w WHERE w.status = :waitQueueStatus ORDER BY w.id ASC LIMIT :passSize")
    List<WaitQueue> findByStatusByLimit(WaitQueueStatus waitQueueStatus, Integer passSize);

    List<WaitQueue> findByStatusAndUpdatedAtBefore(WaitQueueStatus status, LocalDateTime expiredAt);
}
