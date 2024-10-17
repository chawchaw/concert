package com.chaw.concert.app.infrastructure.mysql.conert.reserve;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReserveJpaRepository extends JpaRepository<Reserve, Long> {

    List<Reserve> findByReserveStatusAndCreatedAtBefore(ReserveStatus reserveStatus, LocalDateTime createdAt);

    Reserve findByTicketId(Long ticketId);

    @Query("SELECT r FROM Reserve r WHERE r.userId = :userId AND r.ticketId = :ticketId ORDER BY r.id DESC LIMIT :limit")
    Reserve findByUserIdAndTicketIdOrderByIdDescLimit(Long userId, Long ticketId, Integer limit);
}
