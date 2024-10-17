package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReserved;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RequestReserve {
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;

    public RequestReserve(TicketRepository ticketRepository, ReserveRepository reserveRepository) {
        this.ticketRepository = ticketRepository;
        this.reserveRepository = reserveRepository;
    }

    @Transactional
    public Output execute(Input input) {
        Ticket ticket = ticketRepository.findByIdWithLock(input.ticketId());
        if (ticket == null) {
            throw new TicketNotFound();
        }
        if (!ticket.getStatus().equals(TicketStatus.EMPTY)) {
            throw new TicketAlreadyReserved();
        }

        ticket.setStatus(TicketStatus.RESERVE);
        ticket.setReserveUserId(input.userId());
        ticketRepository.save(ticket);

        Reserve reserve = Reserve.builder()
                .userId(input.userId())
                .ticketId(ticket.getId())
                .reserveStatus(ReserveStatus.RESERVE)
                .amount(ticket.getPrice())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        reserveRepository.save(reserve);
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
