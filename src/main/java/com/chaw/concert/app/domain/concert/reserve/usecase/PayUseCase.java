package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.entity.PaymentMethod;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PayEventRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.PayEvent;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.concert.app.infrastructure.redis.helper.RedissonRLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Slf4j
@AllArgsConstructor
@Service
public class PayUseCase {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final PaymentRepository paymentRepository;
    private final PaidTicketRepository paidTicketRepository;
    private final PayEventRepository payEventRepository;

    @RedissonRLock(key = Point.REDIS_LOCK_KEY)
    public Output execute(Input input) {
        Point point = pointRepository.findByUserId(input.userId());
        Ticket ticket = ticketRepository.findByIdOrThrow(input.ticketId());
        ConcertSchedule concertSchedule = concertScheduleRepository.findByIdOrThrow(ticket.getConcertScheduleId());

        // 검증 시작 ======
        Boolean isReserved = reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(
                ticket.getConcertScheduleId(),
                input.ticketId(),
                input.userId());
        if (!isReserved) {
            throw new BaseException(ErrorType.CONFLICT, "예약되지 않은 티켓입니다.");
        }
        Boolean isPaid = paymentRepository.existsByTicketId(input.ticketId());
        if (isPaid) {
            throw new BaseException(ErrorType.CONFLICT, "이미 결제된 티켓입니다.");
        }
        point.validateHasEnoughBalanceOrThrow(ticket.getPrice());
        // ====== 검증 완료

        // (예약가능 좌석수, 재고없음) 업데이트
        boolean didDecreaseAvailableSeat = concertScheduleRepository.decreaseAvailableSeat(concertSchedule.getId());
        if (!didDecreaseAvailableSeat) {
            throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, MessageFormat.format("남은 좌석이 없습니다. 일정({0}), 티켓({1}) ", concertSchedule.getId(), ticket.getId()));
        }

        // 포인트 차감
        point.decreaseBalance(ticket.getPrice());
        pointRepository.save(point);

        // 포인트 히스토리 추가
        PointHistory pointHistory = PointHistory.createPay(point.getId(), ticket.getId(), ticket.getPrice());
        pointHistoryRepository.save(pointHistory);

        // 결제 추가
        Payment payment = Payment.create(input.userId(), ticket.getConcertScheduleId(), ticket.getId(), pointHistory.getId(), PaymentMethod.POINT, ticket.getPrice());
        paymentRepository.save(payment);

        paidTicketRepository.save(ticket.getConcertScheduleId(), ticket.getId());

        log.info("결제({}) 완료", payment.getId());
        payEventRepository.complete(new PayEvent(payment.getConcertScheduleId(), payment.getTicketId(), payment.getUserId()));
        return new Output(true, payment.getId(), point.getBalance());
    }

    public record Input (
        Long userId,
        Long ticketId
    ) {}

    public record Output (
        Boolean success,
        Long paymentId,
        Integer balance
    ) {}
}
