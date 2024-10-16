package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReserved;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RequestReserve {
    private final TicketRepository ticketRepository;

    public RequestReserve(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Output execute(Input input) {
        Optional<Ticket> ticketOptional = ticketRepository.findByIdWithLock(input.ticketId());
        if (ticketOptional.isEmpty()) {
            throw new TicketNotFound();
        }
        Ticket ticket = ticketOptional.get();
        if (!ticket.getStatus().equals(TicketStatus.EMPTY)) {
            throw new TicketAlreadyReserved();
        }

        ticket.setStatus(TicketStatus.RESERVE);
        ticket.setReserveUserId(input.userId());
        ticket.setReserveEndAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        return new Output(ticket);
    }

    public record Input (
        Long userId,
        Long ticketId
    ) {}

    public record Output (
        Ticket ticket
    ) {}
}
