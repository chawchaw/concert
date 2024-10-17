package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpireReserve {

    private final TicketRepository ticketRepository;

    public ExpireReserve(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * 예약 마감기간이 지난 티켓들을 확인하여 예약을 해제한다.
     */
    public void execute() {
        List<Ticket> tickets = ticketRepository.findByReserveExpired();
        tickets.forEach(ticket -> {
            ticket.setStatus(TicketStatus.EMPTY);
            ticket.setReserveEndAt(null);
            ticket.setReserveUserId(null);
            ticketRepository.save(ticket);
        });
    }
}
