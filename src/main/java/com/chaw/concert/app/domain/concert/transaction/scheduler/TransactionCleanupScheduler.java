package com.chaw.concert.app.domain.concert.transaction.scheduler;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransactionCleanupScheduler {

    private final TicketRepository ticketRepository;
    private final TicketTransactionRepository ticketTransactionRepository;

    public TransactionCleanupScheduler(TicketRepository ticketRepository, TicketTransactionRepository ticketTransactionRepository) {
        this.ticketRepository = ticketRepository;
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

             Ticket ticket = ticketRepository.findById(transaction.getTicketId());
             if (ticket.getStatus() == TicketStatus.RESERVE) {
                 ticket.setStatus(TicketStatus.EMPTY);
                 ticket.setReserveUserId(null);
                 ticket.setReserveEndAt(null);
                 ticketRepository.save(ticket);
             }
        }
    }
}
