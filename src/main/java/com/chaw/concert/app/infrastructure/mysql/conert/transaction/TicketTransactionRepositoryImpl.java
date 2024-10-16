package com.chaw.concert.app.infrastructure.mysql.conert.transaction;

import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TicketTransactionRepositoryImpl implements TicketTransactionRepository {

    private final TicketTransactionJpaRepository repository;

    public TicketTransactionRepositoryImpl(TicketTransactionJpaRepository ticketTransactionJpaRepository) {
        this.repository = ticketTransactionJpaRepository;
    }

    @Override
    public TicketTransaction save(TicketTransaction ticketTransaction) {
        return repository.save(ticketTransaction);
    }

    @Override
    public TicketTransaction findByUserIdAndTicketIdAndIsDeleted(Long userId, Long ticketId, Boolean isDeleted) {
        return repository.findByUserIdAndTicketIdAndIsDeleted(userId, ticketId, isDeleted);
    }

    @Override
    public TicketTransaction findByIdempotencyKeyAndIsDeletedWithLock(String idempotencyKey, Boolean isDeleted) {
        return repository.findByIdempotencyKeyAndIsDeletedWithLock(idempotencyKey, isDeleted);
    }

    @Override
    public List<TicketTransaction> findByTransactionStatusNotCompletedAndExpiredAtBeforeAndNotDeleted(LocalDateTime cutoffTime) {
        return repository.findByTransactionStatusNotCompletedAndExpiredAtBeforeAndIsDeleted(cutoffTime, false);
    }

    @Override
    public List<TicketTransaction> findByTransactionStatus(TransactionStatus transactionStatus) {
        return repository.findByTransactionStatus(transactionStatus);
    }

    @Override
    public TicketTransaction findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
