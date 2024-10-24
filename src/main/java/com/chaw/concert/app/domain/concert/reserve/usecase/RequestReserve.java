package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.ConcertNotFoundException;
import com.chaw.concert.app.domain.concert.query.exception.ConcertScheduleNotFoundException;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReservedException;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFoundException;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.exception.IllegalConcertAndScheduleException;
import com.chaw.concert.app.domain.concert.reserve.exception.IllegalScheduleAndTicketException;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.validation.ReserveValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RequestReserve {

    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final ReserveValidation reserveValidation;

    public RequestReserve(ConcertRepository concertRepository, ConcertScheduleRepository concertScheduleRepository, TicketRepository ticketRepository, ReserveRepository reserveRepository, ReserveValidation reserveValidation) {
        this.concertRepository = concertRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.ticketRepository = ticketRepository;
        this.reserveRepository = reserveRepository;
        this.reserveValidation = reserveValidation;
    }

    @Transactional
    public Output execute(Input input) {
        Concert concert = concertRepository.findById(input.concertId());
        ConcertSchedule concertSchedule = concertScheduleRepository.findById(input.concertScheduleId());
        Ticket ticket = ticketRepository.findByIdWithLock(input.ticketId());

        reserveValidation.validateConcertDetails(concert, concertSchedule, ticket);
        reserveValidation.validateReserveDetails(ticket);

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
        Long concertId,
        Long concertScheduleId,
        Long ticketId
    ) {}

    public record Output (
        Ticket ticket
    ) {}
}
