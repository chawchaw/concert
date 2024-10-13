package com.chaw.concert.app.infrastructure.mysql.conert.queue;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaitQueueJpaRepository extends JpaRepository<WaitQueue, Long> {
    Boolean existsByConcertIdAndUuid(Long concertId, String uuid);

    List<WaitQueue> findByConcertIdAndIdGreaterThanOrderByIdAsc(Long concertId, Long lastTransferredWaitingUserId);
}
