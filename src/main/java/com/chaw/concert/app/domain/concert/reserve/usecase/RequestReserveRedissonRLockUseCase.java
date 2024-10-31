package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Service
public class RequestReserveRedissonRLockUseCase {

    private final String REDIS_LOCK_KEY = "request-reserve-lock-";

    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Output execute(Input input) {
        String lockKey = REDIS_LOCK_KEY + input.ticketId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean lockAcquired = lock.tryLock(1L, 1L, TimeUnit.SECONDS);
            if (!lockAcquired) {
                log.warn("티켓 {} Redisson RLock 획득 실패", input.ticketId());
                throw new BaseException(ErrorType.CONFLICT, "Redisson 락 획득 실패");
            }

            try {
                return reserveWithTransaction(input);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            log.error("티켓 {} Redisson RLock 획득 중 인터럽트 발생", input.ticketId(), e);
            throw new BaseException(ErrorType.CONFLICT, "Redisson 락 획득 중 인터럽트 발생");
        }
    }

    public Output reserveWithTransaction(Input input) {
        return transactionTemplate.execute(status -> {
            Ticket ticket = ticketRepository.findByIdOrThrow(input.ticketId());
            ticket.isReservableOrThrow();

            ticket.reserveWithUserId(input.userId());
            ticketRepository.save(ticket);

            Reserve reserve = Reserve.create(input.userId(), ticket.getId(), ticket.getPrice());
            reserveRepository.save(reserve);

            entityManager.flush();

            log.info("예약({}) 완료", reserve.getId());
            return new Output(true);
        });
    }

    public record Input (
            Long userId,
            Long ticketId
    ) {}

    public record Output (
            Boolean success
    ) {}
}
