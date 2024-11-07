package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class GetTicketsInEmptyStatusUseCase {

    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final PaidTicketRepository paidTicketRepository;

    @Cacheable(value = "ticketsInEmpty", key = "#input.concertScheduleId()", unless = "#result.tickets.isEmpty()")
    public Output execute(Input input) {
        List<Ticket> tickets = new ArrayList<>(ticketRepository.findByConcertScheduleIdWithCache(input.concertScheduleId()));
        Set<Long> reservedTickets = reserveRepository.findByConcertScheduleId(input.concertScheduleId());
        Set<Long> paidTickets = paidTicketRepository.findByConcertScheduleId(input.concertScheduleId());

        tickets.removeIf(ticket -> reservedTickets.contains(ticket.getId()));
        tickets.removeIf(ticket -> paidTickets.contains(ticket.getId()));

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
    ) implements Serializable {
        public record Item (
            Long id,
            String type,
            String seatNo,
            Integer price
        ) implements Serializable {}
    }
}
