package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReserveEvent;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.concert.app.infrastructure.redis.helper.RedissonRLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class RequestReserveRedissonRLockUseCase {

    private static final String REDIS_LOCK_KEY = "'request-reserve'.concat(#input.ticketId().toString())";

    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @RedissonRLock(key = REDIS_LOCK_KEY, waitTime = 0)
    public Output execute(Input input) {
        Ticket ticket = ticketRepository.findByIdOrThrow(input.ticketId());

        Boolean isReserved = reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(
                ticket.getConcertScheduleId(),
                input.ticketId(),
                input.userId());
        if (isReserved) {
            throw new BaseException(ErrorType.CONFLICT, "이미 예약된 티켓입니다.");
        }
        Boolean isPaid = paymentRepository.existsByTicketId(input.ticketId());
        if (isPaid) {
            throw new BaseException(ErrorType.CONFLICT, "이미 결제된 티켓입니다.");
        }

        Reserve reserve = new Reserve(ticket.getConcertScheduleId(), input.ticketId(), input.userId());
        reserveRepository.save(reserve);

        log.info("예약({}) 완료", input.ticketId());
        eventPublisher.publishEvent(new ReserveEvent(reserve.concertScheduleId(), reserve.ticketId(), reserve.userId()));
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
