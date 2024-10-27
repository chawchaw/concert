package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RequestReserveUseCase {

    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;

    public RequestReserveUseCase(TicketRepository ticketRepository, ReserveRepository reserveRepository) {
        this.ticketRepository = ticketRepository;
        this.reserveRepository = reserveRepository;
    }

    @Transactional
    public Output execute(Input input) {
        Ticket ticket = ticketRepository.findByIdWithLockOrThrow(input.ticketId());
        ticket.isReservableOrThrow();

        ticket.reserveWithUserId(input.userId());
        ticketRepository.save(ticket);

        Reserve reserve = Reserve.create(input.userId(), ticket.getId(), ticket.getPrice());
        reserveRepository.save(reserve);

        log.info("예약({}) 완료", reserve.getId());
        return new Output(true);
    }

    public record Input (
        Long userId,
        Long ticketId
    ) {}

    public record Output (
        Boolean success
    ) {}
}
