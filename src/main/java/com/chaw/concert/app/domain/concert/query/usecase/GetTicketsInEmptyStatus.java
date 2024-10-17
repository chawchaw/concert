package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTicketsInEmptyStatus {

    private final TicketRepository ticketRepository;

    public GetTicketsInEmptyStatus(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Output execute(Input input) {
        List<Ticket> tickets = ticketRepository.findByConcertScheduleIdAndStatus(input.concertScheduleId(), TicketStatus.EMPTY);
        return new Output(
                input.concertScheduleId(),
                tickets.stream().map(ticket -> new Output.Item(
                        ticket.getId(),
                        ticket.getType().name(),
                        ticket.getSeatNo(),
                        ticket.getPrice()
                )).toList()
        );
    }

    public record Input (
        Long concertScheduleId
    ) {}

    public record Output (
            Long concertScheduleId,
            List<Item> tickets
    ) {
        public record Item (
            Long id,
            String type,
            String seatNo,
            Integer price
        ) {}
    }
}
