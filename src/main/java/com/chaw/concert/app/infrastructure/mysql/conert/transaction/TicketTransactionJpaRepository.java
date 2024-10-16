package com.chaw.concert.app.infrastructure.mysql.conert.transaction;

import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketTransactionJpaRepository extends JpaRepository<TicketTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketTransaction t WHERE t.idempotencyKey = :idempotencyKey AND t.isDeleted = :isDeleted")
    TicketTransaction findByIdempotencyKeyAndIsDeletedWithLock(String idempotencyKey, Boolean isDeleted);

    TicketTransaction findByUserIdAndTicketIdAndIsDeleted(Long userId, Long ticketId, Boolean isDeleted);

    @Query("SELECT t FROM TicketTransaction t WHERE t.transactionStatus <> 'COMPLETED' AND t.expiredAt < :cutoffTime AND t.isDeleted = :isDeleted")
    List<TicketTransaction> findByTransactionStatusNotCompletedAndExpiredAtBeforeAndIsDeleted(LocalDateTime cutoffTime, Boolean isDeleted);

    List<TicketTransaction> findByTransactionStatus(TransactionStatus transactionStatus);
}
