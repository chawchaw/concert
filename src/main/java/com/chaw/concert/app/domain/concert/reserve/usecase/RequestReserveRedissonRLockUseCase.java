package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.infrastructure.redis.helper.RedissonRLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class RequestReserveRedissonRLockUseCase {

    private final String REDIS_LOCK_KEY = "'request-reserve'.concat(#input.ticketId().toString())";

    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;

    @RedissonRLock(key = REDIS_LOCK_KEY, waitTime = 0)
    public Output execute(Input input) {
        Ticket ticket = ticketRepository.findByIdOrThrow(input.ticketId());
        ticket.isReservableOrThrow();

        ticket.reserve();
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
