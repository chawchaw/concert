package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.ConcertOutbox;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ConcertOutboxRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReservedEventRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.concert.app.infrastructure.redis.helper.RedissonRLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReserveUseCase {

    private static final String REDIS_LOCK_KEY = "'request-reserve'.concat(#input.ticketId().toString())";

    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final PaymentRepository paymentRepository;
    private final ConcertOutboxRepository concertOutboxRepository;
    private final ReservedEventRepository reservedEventRepository;

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

        // Redis 에 예약 정보 저장
        Reserve reserve = new Reserve(ticket.getConcertScheduleId(), input.ticketId(), input.userId());
        reserveRepository.save(reserve);

        // Kafka Outbox 에 이벤트 저장
        ConcertOutbox concertOutbox = ConcertOutbox.createReserved(ticket.getConcertScheduleId(), input.ticketId(), input.userId());
        concertOutboxRepository.save(concertOutbox);

        // Kafka Producer 로 이벤트 발행
        reservedEventRepository.complete(ReservedEvent.builder()
                .concertScheduleId(ticket.getConcertScheduleId())
                .ticketId(input.ticketId())
                .userId(input.userId())
                .concertOutboxId(concertOutbox.getId())
                .build());

        log.info("예약({}) 완료", input.ticketId());

        return new Output(true, concertOutbox.getId());
    }

    public record Input (
            Long userId,
            Long ticketId
    ) {}

    public record Output (
            Boolean success,
            Long concertOutboxId
    ) {}
}
