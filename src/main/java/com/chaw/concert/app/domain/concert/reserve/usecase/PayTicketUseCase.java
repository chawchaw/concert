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
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;

@Service
@Slf4j
public class PayTicketUseCase {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final PaymentRepository paymentRepository;

    public PayTicketUseCase(PointRepository pointRepository, PointHistoryRepository pointHistoryRepository, ConcertScheduleRepository concertScheduleRepository, TicketRepository ticketRepository, ReserveRepository reserveRepository, PaymentRepository paymentRepository) {
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.ticketRepository = ticketRepository;
        this.reserveRepository = reserveRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Output execute(Input input) {
        Point point = pointRepository.findByUserIdWithLock(input.userId()); // 중복 결제 방지를 위해 비관 락 사용
        Ticket ticket = ticketRepository.findByIdOrThrow(input.ticketId());
        ConcertSchedule concertSchedule = concertScheduleRepository.findByIdWithLockThrow(ticket.getConcertScheduleId()); // 예약 가능 좌석 수 업데이트를 위해 비관 락 사용
        Reserve reserve = reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(input.userId(), input.ticketId(), 1);

        point.validateHasEnoughBalanceOrThrow(reserve.getAmount());
        ticket.isPayableOrThrow();
        reserve.isReservableStatusOrThrow();
        reserve.isExpiredThenDoAndThrow(() -> {
            ticket.resetToEmpty();
            ticketRepository.save(ticket);

            reserve.cancel();
            reserveRepository.save(reserve);
        });

        // (예약가능 좌석수, 재고없음) 업데이트
        boolean result = concertScheduleRepository.decreaseAvailableSeat(concertSchedule.getId());
        if (!result) {
            throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, MessageFormat.format("남은 좌석이 없습니다. 일정({0}), 티켓({1}) ", concertSchedule.getId(), ticket.getId()));
        }

        // 티켓 상태 업데이트
        ticket.pay();
        ticketRepository.save(ticket);

        // 예약 상태 업데이트
        reserve.pay();
        reserveRepository.save(reserve);

        // 포인트 차감
        point.decreaseBalance(reserve.getAmount());
        pointRepository.save(point);

        // 포인트 히스토리 추가
        PointHistory pointHistory = PointHistory.create(point.getId(), ticket.getId(), reserve.getAmount());
        pointHistoryRepository.save(pointHistory);

        // 결제 추가
        Payment payment = Payment.create(input.userId(), reserve.getId(), pointHistory.getId(), PaymentMethod.POINT, reserve.getAmount());
        paymentRepository.save(payment);

        log.info("결제({}) 완료", payment.getId());
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
