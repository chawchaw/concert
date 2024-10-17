//package com.chaw.concert.app.domain.concert.reserve.usecase;
//
//import com.chaw.concert.app.domain.common.user.entity.Point;
//import com.chaw.concert.app.domain.common.user.entity.PointHistory;
//import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
//import com.chaw.concert.app.domain.common.user.exception.PointNotFound;
//import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
//import com.chaw.concert.app.domain.common.user.repository.PointRepository;
//import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
//import com.chaw.concert.app.domain.concert.query.entity.Ticket;
//import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
//import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
//import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
//import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
//import com.chaw.concert.app.domain.concert.reserve.entity.PaymentMethod;
//import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
//import com.chaw.concert.app.domain.concert.reserve.entity.TransactionStatus;
//import com.chaw.concert.app.domain.concert.reserve.exception.IdempotencyNotFound;
//import com.chaw.concert.app.domain.concert.reserve.exception.TicketNotInStatusReserve;
//import com.chaw.concert.app.domain.concert.reserve.repository.TicketTransactionRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//public class PayTicket {
//
//    private final ConcertScheduleRepository concertScheduleRepository;
//    private final TicketTransactionRepository ticketTransactionRepository;
//    private final PointHistoryRepository pointHistoryRepository;
//    private final PointRepository pointRepository;
//    private final TicketRepository ticketRepository;
//
//    public PayTicket(ConcertScheduleRepository concertScheduleRepository, TicketTransactionRepository ticketTransactionRepository, PointHistoryRepository pointHistoryRepository, PointRepository pointRepository, TicketRepository ticketRepository) {
//        this.concertScheduleRepository = concertScheduleRepository;
//        this.ticketTransactionRepository = ticketTransactionRepository;
//        this.pointHistoryRepository = pointHistoryRepository;
//        this.pointRepository = pointRepository;
//        this.ticketRepository = ticketRepository;
//    }
//
//    @Transactional
//    public Output execute(Input input) {
//        LocalDateTime now = LocalDateTime.now();
//
//        Reserve reserve = ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(input.idempotencyKey(), false);
//        if (reserve == null) {
//            throw new IdempotencyNotFound();
//        }
//        if (now.isAfter(reserve.getExpiredAt())) {
//            reserve.setTransactionStatus(TransactionStatus.EXPIRED);
//            reserve.setIsDeleted(true);
//            ticketTransactionRepository.save(reserve);
//            return new Output(reserve.getTransactionStatus().name(), "결제 유효기간이 만료되었습니다.");
//        }
//
//        Ticket ticket = ticketRepository.findById(reserve.getTicketId());
//        if (ticket == null) {
//            throw new TicketNotFound();
//        } else if (ticket.getStatus() != TicketStatus.RESERVE) {
//            throw new TicketNotInStatusReserve();
//        }
//
//        Point point = pointRepository.findByUserIdWithLock(reserve.getUserId());
//        if (point == null) {
//            throw new PointNotFound();
//        }
//
//        if (reserve.getTransactionStatus() == TransactionStatus.COMPLETED) {
//            return new Output(reserve.getTransactionStatus().name(), "이미 결제가 완료되었습니다.");
//        }
//
//        if (point.getBalance() < reserve.getAmount()) {
//            reserve.setTransactionStatus(TransactionStatus.FAILED);
//            ticketTransactionRepository.save(reserve);
//            return new Output(reserve.getTransactionStatus().name(), "잔액이 부족합니다.");
//        }
//
//        reserve.setPaymentMethod(PaymentMethod.POINT);
//        reserve.setTransactionStatus(TransactionStatus.COMPLETED);
//        reserve.setUpdatedAt(now);
//        ticketTransactionRepository.save(reserve);
//
//        PointHistory pointHistory = PointHistory.builder()
//                .pointId(point.getId())
//                .ticketId(reserve.getTicketId())
//                .type(PointHistoryType.PAY)
//                .amount(reserve.getAmount())
//                .dateTransaction(now)
//                .build();
//        pointHistoryRepository.save(pointHistory);
//
//        Integer changedAmount = pointHistory.getChangedAmount();
//        Integer balance = point.getBalance() + changedAmount;
//        point.setBalance(balance);
//        pointRepository.save(point);
//
//        ticket.setStatus(TicketStatus.PAID);
//        ticketRepository.save(ticket);
//
//        ConcertSchedule concertSchedule = concertScheduleRepository.findByIdWithLock(ticket.getConcertScheduleId());
//        concertSchedule.setAvailableSeat(concertSchedule.getAvailableSeat() - 1);
//        if (concertSchedule.getAvailableSeat() == 0) {
//            concertSchedule.setIsSold(true);
//        }
//        concertScheduleRepository.save(concertSchedule);
//
//        return new Output(reserve.getTransactionStatus().name(), "결제가 완료되었습니다. 잔액: " + balance);
//    }
//
//    public record Input (
//        String idempotencyKey,
//        Long userId
//    ) {}
//
//    public record Output (
//        String status,
//        String message
//    ) {}
//}
