package com.chaw.concert.app.domain.concert.transaction.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.exception.TicketNotInStatusReserve;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RequestIdempotencyKey {

    @Value("${concert.transaction.expired.minutes}")
    private Integer EXPIRED_MINUTES;

    private final TicketRepository ticketRepository;
    private final TicketTransactionRepository ticketTransactionRepository;

    public RequestIdempotencyKey(TicketRepository ticketRepository, TicketTransactionRepository ticketTransactionRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketTransactionRepository = ticketTransactionRepository;
    }

    public Output execute(Input input) {
        Ticket ticket = ticketRepository.findById(input.ticketId());
        if (ticket == null) {
            throw new TicketNotFound();
        } else if (ticket.getStatus() != TicketStatus.RESERVE) {
            throw new TicketNotInStatusReserve();
        }

        TicketTransaction ticketTransaction = ticketTransactionRepository.findByUserIdAndTicketIdAndIsDeleted(input.userId(), input.ticketId(), false);
        if (ticketTransaction == null) {
            LocalDateTime now = LocalDateTime.now();
            ticketTransaction = TicketTransaction.builder()
                    .ticketId(input.ticketId())
                    .userId(input.userId())
                    .idempotencyKey(UUID.randomUUID().toString())
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(ticket.getPrice())
                    .createdAt(now)
                    .updatedAt(now)
                    .expiredAt(now.plusMinutes(EXPIRED_MINUTES))
                    .isDeleted(false)
                    .build();
            ticketTransactionRepository.save(ticketTransaction);
        }

        return new Output(ticketTransaction.getIdempotencyKey(), ticketTransaction.getTransactionStatus().name());
    }

    public record Input (
        Long ticketId,
        Long userId
    ) {}

    public record Output (
        String idempotencyKey,
        String status
    ) {}
}
