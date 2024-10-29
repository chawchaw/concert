package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class RequestReserveOptimisticLockUseCase {

    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Output execute(Input input) {
        Ticket ticket = ticketRepository.findByIdOrThrow(input.ticketId());
        try {
            ticket.isReservableOrThrow();
            ticket.reserveWithUserId(input.userId());
            ticketRepository.save(ticket);

            Reserve reserve = Reserve.create(input.userId(), ticket.getId(), ticket.getPrice());
            reserveRepository.save(reserve);

            entityManager.flush();

            log.info("예약({}) 완료", reserve.getId());
            return new Output(true);
        } catch (OptimisticLockException e) {
            log.error("낙관락 실패");
            throw ticket.getExceptionForNotReservable();
        }
    }

    public record Input (
            Long userId,
            Long ticketId
    ) {}

    public record Output (
            Boolean success
    ) {}
}
