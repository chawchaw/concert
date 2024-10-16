package com.chaw.concert.app.domain.concert.transaction.scheduler;

import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransactionCleanupScheduler {

    private final TicketTransactionRepository ticketTransactionRepository;

    public TransactionCleanupScheduler(TicketTransactionRepository ticketTransactionRepository) {
        this.ticketTransactionRepository = ticketTransactionRepository;
    }

    @Scheduled(cron = "0 * * * * *")
    public void cleanupExpiredTransactions() {
        LocalDateTime cutoffTime = LocalDateTime.now();
        List<TicketTransaction> expiredTransactions = ticketTransactionRepository.findByTransactionStatusNotCompletedAndExpiredAtBeforeAndNotDeleted(cutoffTime);

        for (TicketTransaction transaction : expiredTransactions) {
             transaction.setTransactionStatus(TransactionStatus.EXPIRED);
             transaction.setIsDeleted(true);
             ticketTransactionRepository.save(transaction);
        }
    }
}
