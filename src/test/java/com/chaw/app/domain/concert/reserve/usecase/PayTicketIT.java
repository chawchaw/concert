package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
public class PayTicketIT {

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayTicket payTicket;

    private Long userId = 1L;
    private Integer balance = 1000;
    private Integer price = 100;

    private Point point;
    private Concert concert;
    private ConcertSchedule concertSchedule;
    private WaitQueue waitQueue;
    private Ticket ticket;
    private Reserve reserve;

    @BeforeEach
    void setUp() {
        point = Point.builder()
                .userId(userId)
                .balance(balance)
                .build();
        pointRepository.save(point);

        concert = Concert.builder()
                .name("concert")
                .build();
        concertRepository.save(concert);

        concertSchedule = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSoldOut(false)
                .totalSeat(10)
                .availableSeat(10)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule);

        waitQueue = WaitQueue.builder()
                .userId(userId)
                .status(WaitQueueStatus.PASS)
                .build();
        waitQueueRepository.save(waitQueue);

        ticket = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
                .status(TicketStatus.RESERVE)
                .price(price)
                .reserveUserId(userId)
                .build();
        ticketRepository.save(ticket);

        reserve = Reserve.builder()
                .userId(userId)
                .ticketId(ticket.getId())
                .reserveStatus(ReserveStatus.RESERVE)
                .amount(ticket.getPrice())
                .createdAt(LocalDateTime.now())
                .build();
        reserveRepository.save(reserve);
    }

    @AfterEach
    void tearDown() {
        waitQueueRepository.deleteAll();
        pointRepository.deleteAll();
        pointHistoryRepository.deleteAll();
        concertRepository.deleteAll();
        concertScheduleRepository.deleteAll();
        ticketRepository.deleteAll();
        reserveRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void payTicketSuccess() {
        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());
        PayTicket.Output output = payTicket.execute(input);

        ConcertSchedule concertScheduleAfter = concertScheduleRepository.findById(concertSchedule.getId());
        Ticket ticketAfter = ticketRepository.findById(ticket.getId());
        Reserve reserveAfter = reserveRepository.findById(reserve.getId());
        Point pointAfter = pointRepository.findByUserId(userId);
        Payment payment = paymentRepository.findById(output.paymentId());
        PointHistory pointHistory = pointHistoryRepository.findById(payment.getPointHistoryId());

        assertEquals(true, output.success());
        assertNotNull(output.paymentId());
        assertEquals(1000 - 100, output.balance());

        assertEquals(9, concertScheduleAfter.getAvailableSeat());
        assertEquals(false, concertScheduleAfter.getIsSoldOut());
        assertEquals(TicketStatus.PAID, ticketAfter.getStatus());
        assertEquals(ReserveStatus.PAID, reserveAfter.getReserveStatus());
        assertEquals(900, pointAfter.getBalance());
        assertEquals(100, pointHistory.getAmount());
        assertEquals(100, payment.getAmount());
    }

    @Test
    void payTicketSuccessSoldOut() {
        concertSchedule.limitAvailableSeatsToOne();
        concertScheduleRepository.save(concertSchedule);

        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());
        PayTicket.Output output = payTicket.execute(input);

        ConcertSchedule concertScheduleAfter = concertScheduleRepository.findById(concertSchedule.getId());

        assertEquals(true, output.success());

        assertEquals(0, concertScheduleAfter.getAvailableSeat());
        assertEquals(true, concertScheduleAfter.getIsSoldOut());
    }

    @Test
    void validate_NotEnoughBalance() {
        point = Point.builder()
                .id(point.getId())
                .userId(userId)
                .balance(50)
                .build();
        pointRepository.save(point);
        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());

        BaseException exception = assertThrows(BaseException.class, () -> { payTicket.execute(input); });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }

    @Test
    void validate_TicketNotInStatusReserve() {
        ticket.pay();
        ticketRepository.save(ticket);
        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());

        BaseException exception = assertThrows(BaseException.class, () -> { payTicket.execute(input); });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }

    @Test
    void validate_AlreadyPaidReserve() {
        reserve.pay();
        reserveRepository.save(reserve);
        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());

        BaseException baseException = assertThrows(BaseException.class, () -> { payTicket.execute(input); });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }

    @Test
    void validate_CanceledReserve() {
        reserve.cancel();
        reserveRepository.save(reserve);
        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());

        BaseException baseException = assertThrows(BaseException.class, () -> { payTicket.execute(input); });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }

    @Test
    void validate_AvailableSeatNotExist() {
        concertSchedule.limitAvailableSeatsToZero();
        concertScheduleRepository.save(concertSchedule);

        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());

        BaseException baseException = assertThrows(BaseException.class, () -> { payTicket.execute(input); });
        assertEquals(ErrorType.DATA_INTEGRITY_VIOLATION, baseException.getErrorType());
    }

    @Test
    void validate_ExpiredReserve() {
        reserve.setCreationTimeToPast(31);
        reserveRepository.save(reserve);
        PayTicket.Input input = new PayTicket.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());

        BaseException baseException = assertThrows(BaseException.class, () -> { payTicket.execute(input); });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }
}
