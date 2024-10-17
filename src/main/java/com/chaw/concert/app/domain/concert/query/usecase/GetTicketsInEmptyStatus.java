package com.chaw.concert.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.ConcertNotFoundException;
import com.chaw.concert.app.domain.concert.query.exception.ConcertScheduleNotFoundException;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTicketsInEmptyStatus {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final TicketRepository ticketRepository;

    public GetTicketsInEmptyStatus(ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository, TicketRepository ticketRepository) {
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.ticketRepository = ticketRepository;
    }

    public Output execute(Input input) {
        Concert concert = concertRepository.findById(input.concertId());
        ConcertSchedule concertSchedule = concertScheduleRepository.findById(input.concertScheduleId());

        if (concert == null) {
            throw new ConcertNotFoundException();
        }
        if (concertSchedule == null) {
            throw new ConcertScheduleNotFoundException();
        }

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
            Long concertId,
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
