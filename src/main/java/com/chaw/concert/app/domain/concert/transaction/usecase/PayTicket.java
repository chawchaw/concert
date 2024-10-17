package com.chaw.concert.app.domain.concert.transaction.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
import com.chaw.concert.app.domain.common.user.exception.PointNotFound;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.PaymentMethod;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.exception.IdempotencyNotFound;
import com.chaw.concert.app.domain.concert.transaction.exception.TicketNotInStatusReserve;
import com.chaw.concert.app.domain.concert.transaction.exception.TransactionExpired;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PayTicket {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final TicketTransactionRepository ticketTransactionRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointRepository pointRepository;
    private final TicketRepository ticketRepository;

    public PayTicket(ConcertScheduleRepository concertScheduleRepository, TicketTransactionRepository ticketTransactionRepository, PointHistoryRepository pointHistoryRepository, PointRepository pointRepository, TicketRepository ticketRepository) {
        this.concertScheduleRepository = concertScheduleRepository;
        this.ticketTransactionRepository = ticketTransactionRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.pointRepository = pointRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Output execute(Input input) {
        LocalDateTime now = LocalDateTime.now();

        TicketTransaction ticketTransaction = ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(input.idempotencyKey(), false);
        if (ticketTransaction == null) {
            throw new IdempotencyNotFound();
        }
        if (now.isAfter(ticketTransaction.getExpiredAt())) {
            ticketTransaction.setTransactionStatus(TransactionStatus.EXPIRED);
            ticketTransaction.setIsDeleted(true);
            ticketTransactionRepository.save(ticketTransaction);
            return new Output(ticketTransaction.getTransactionStatus().name(), "결제 유효기간이 만료되었습니다.");
        }

        Ticket ticket = ticketRepository.findById(ticketTransaction.getTicketId());
        if (ticket == null) {
            throw new TicketNotFound();
        } else if (ticket.getStatus() != TicketStatus.RESERVE) {
            throw new TicketNotInStatusReserve();
        }

        Point point = pointRepository.findByUserIdWithLock(ticketTransaction.getUserId());
        if (point == null) {
            throw new PointNotFound();
        }

        if (ticketTransaction.getTransactionStatus() == TransactionStatus.COMPLETED) {
            return new Output(ticketTransaction.getTransactionStatus().name(), "이미 결제가 완료되었습니다.");
        }

        if (point.getBalance() < ticketTransaction.getAmount()) {
            ticketTransaction.setTransactionStatus(TransactionStatus.FAILED);
            ticketTransactionRepository.save(ticketTransaction);
            return new Output(ticketTransaction.getTransactionStatus().name(), "잔액이 부족합니다.");
        }

        ticketTransaction.setPaymentMethod(PaymentMethod.POINT);
        ticketTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        ticketTransaction.setUpdatedAt(now);
        ticketTransactionRepository.save(ticketTransaction);

        PointHistory pointHistory = PointHistory.builder()
                .pointId(point.getId())
                .ticketId(ticketTransaction.getTicketId())
                .type(PointHistoryType.PAY)
                .amount(ticketTransaction.getAmount())
                .dateTransaction(now)
                .build();
        pointHistoryRepository.save(pointHistory);

        Integer changedAmount = pointHistory.getChangedAmount();
        Integer balance = point.getBalance() + changedAmount;
        point.setBalance(balance);
        pointRepository.save(point);

        ticket.setStatus(TicketStatus.PAID);
        ticketRepository.save(ticket);

        ConcertSchedule concertSchedule = concertScheduleRepository.findByIdWithLock(ticket.getConcertScheduleId());
        concertSchedule.setAvailableSeat(concertSchedule.getAvailableSeat() - 1);
        if (concertSchedule.getAvailableSeat() == 0) {
            concertSchedule.setIsSold(true);
        }
        concertScheduleRepository.save(concertSchedule);

        return new Output(ticketTransaction.getTransactionStatus().name(), "결제가 완료되었습니다. 잔액: " + balance);
    }

    public record Input (
        String idempotencyKey,
        Long userId
    ) {}

    public record Output (
        String status,
        String message
    ) {}
}
