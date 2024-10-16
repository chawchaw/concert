package com.chaw.concert.app.domain.concert.transaction.repository;

import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketTransactionRepository {

    TicketTransaction save(TicketTransaction ticketTransaction);

    TicketTransaction findByUserIdAndTicketIdAndIsDeleted(Long userId, Long ticketId, Boolean isDeleted);

    TicketTransaction findByIdempotencyKeyAndIsDeletedWithLock(String idempotencyKey, Boolean isDeleted);

    List<TicketTransaction> findByTransactionStatusNotCompletedAndExpiredAtBeforeAndNotDeleted(LocalDateTime cutoffTime);

    List<TicketTransaction> findByTransactionStatus(TransactionStatus transactionStatus);

    TicketTransaction findById(Long id);

    void deleteAll();
}
