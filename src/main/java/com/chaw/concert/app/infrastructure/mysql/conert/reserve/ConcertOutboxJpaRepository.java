package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutboxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConcertOutboxJpaRepository extends JpaRepository<ConcertOutbox, Long> {

    ConcertOutbox findByIdAndType(Long id, ConcertOutboxType type);

    @Query("select c " +
            "from ConcertOutbox c " +
            "where " +
            "   c.status in :statuses " +
            "   and c.createdAt < :before")
    List<ConcertOutbox> findAllByStatusInAndCreatedAtBefore(List<ConcertOutboxStatus> statuses, LocalDateTime before);
}
