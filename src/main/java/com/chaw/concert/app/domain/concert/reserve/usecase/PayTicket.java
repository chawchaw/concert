package com.chaw.concert.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.entity.PaymentMethod;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.validation.ReserveValidation;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PayTicket {

    @Value("${concert.reserve.expired.minutes}")
    private Integer EXPIRED_MINUTES;

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ConcertRepository concertRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final TicketRepository ticketRepository;
    private final ReserveRepository reserveRepository;
    private final PaymentRepository paymentRepository;
    private final ReserveValidation reserveValidation;

    public PayTicket(ConcertRepository concertRepository, PointRepository pointRepository, PointHistoryRepository pointHistoryRepository, ConcertScheduleRepository concertScheduleRepository, TicketRepository ticketRepository, ReserveRepository reserveRepository, PaymentRepository paymentRepository, ReserveValidation reserveValidation) {
        this.concertRepository = concertRepository;
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.concertScheduleRepository = concertScheduleRepository;
        this.ticketRepository = ticketRepository;
        this.reserveRepository = reserveRepository;
        this.paymentRepository = paymentRepository;
        this.reserveValidation = reserveValidation;
    }

    @Transactional
    public Output execute(Input input) {
        Point point = pointRepository.findByUserIdWithLock(input.userId()); // 중복 결제 방지를 위해 비관 락 사용
        Concert concert = concertRepository.findById(input.concertId());
        Ticket ticket = ticketRepository.findById(input.ticketId());
        ConcertSchedule concertSchedule = concertScheduleRepository.findByIdWithLock(ticket.getConcertScheduleId()); // 예약 가능 좌석 수 업데이트를 위해 비관 락 사용
        Reserve reserve = reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimit(input.userId(), input.ticketId(), 1);

        reserveValidation.validateConcertDetails(input.userId(), concert, concertSchedule, ticket);
        reserveValidation.validatePayTicketDetails(input.userId(), point, reserve, ticket);

        handleExpiredReserve(ticket, reserve);

        LocalDateTime now = LocalDateTime.now();
        // (예약가능 좌석수, 재고없음) 업데이트
        boolean result = concertScheduleRepository.decreaseAvailableSeat(concertSchedule.getId());
        if (!result) {
            throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "남은 좌석이 없습니다. 일정 ID: " + concertSchedule.getId() + ", 티켓 ID: " + ticket.getId());
        }

        // 티켓 상태 업데이트
        ticket.setStatus(TicketStatus.PAID);
        ticketRepository.save(ticket);

        // 예약 상태 업데이트
        reserve.setReserveStatus(ReserveStatus.PAID);
        reserve.setUpdatedAt(now);
        reserveRepository.save(reserve);

        // 포인트 차감
        point.setBalance(point.getBalance() - reserve.getAmount());
        pointRepository.save(point);

        // 포인트 히스토리 추가
        PointHistory pointHistory = PointHistory.builder()
                .pointId(point.getId())
                .ticketId(ticket.getId())
                .type(PointHistoryType.PAY)
                .amount(reserve.getAmount())
                .dateTransaction(now)
                .build();
        pointHistoryRepository.save(pointHistory);

        // 결제 추가
        Payment payment = Payment.builder()
                .userId(input.userId())
                .reserveId(reserve.getId())
                .pointHistoryId(pointHistory.getId())
                .paymentMethod(PaymentMethod.POINT)
                .amount(reserve.getAmount())
                .createdAt(now)
                .build();
        paymentRepository.save(payment);

        log.info("[사용자id({})] 결제({}) 완료", input.userId(), payment.getId());
        return new Output(true, payment.getId(), point.getBalance());
    }

    // 예약제한시간 체크
    public void handleExpiredReserve(Ticket ticket, Reserve reserve) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(reserve.getCreatedAt().plusMinutes(EXPIRED_MINUTES))) {
            ticket.setStatus(TicketStatus.EMPTY);
            ticket.setReserveUserId(null);
            ticketRepository.save(ticket);

            reserve.setReserveStatus(ReserveStatus.CANCEL);
            reserveRepository.save(reserve);

            throw new BaseException(ErrorType.CONFLICT, "결제 유효기간이 만료되었습니다.");
        }
    }

    public record Input (
        Long userId,
        Long concertId,
        Long concertScheduleId,
        Long ticketId
    ) {}

    public record Output (
        Boolean success,
        Long paymentId,
        Integer balance
    ) {}
}
