package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.exception.NotEnoughBalance;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.exception.AlreadyPaidReserve;
import com.chaw.concert.app.domain.concert.reserve.exception.CanceledReserve;
import com.chaw.concert.app.domain.concert.reserve.exception.ExpiredReserve;
import com.chaw.concert.app.domain.concert.reserve.exception.TicketNotInStatusReserve;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
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

        concertSchedule = ConcertSchedule.builder()
                .concertId(1L)
                .isSold(false)
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
        concertScheduleRepository.deleteAll();
        ticketRepository.deleteAll();
        reserveRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void payTicketSuccess() {
        PayTicket.Input input = new PayTicket.Input(userId, ticket.getId());
        PayTicket.Output output = payTicket.execute(input);

        ConcertSchedule concertScheduleAfter = concertScheduleRepository.findById(concertSchedule.getId());
        Ticket ticketAfter = ticketRepository.findById(ticket.getId());
        Reserve reserveAfter = reserveRepository.findById(reserve.getId());
        Point pointAfter = pointRepository.findByUserId(userId);
        WaitQueue waitQueueAfter = waitQueueRepository.findByUserId(userId);
        Payment payment = paymentRepository.findById(output.paymentId());
        PointHistory pointHistory = pointHistoryRepository.findById(payment.getPointHistoryId());

        assertEquals(true, output.success());
        assertNotNull(output.paymentId());
        assertEquals(1000 - 100, output.balance());

        assertEquals(9, concertScheduleAfter.getAvailableSeat());
        assertEquals(false, concertScheduleAfter.getIsSold());
        assertEquals(TicketStatus.PAID, ticketAfter.getStatus());
        assertEquals(ReserveStatus.PAID, reserveAfter.getReserveStatus());
        assertEquals(900, pointAfter.getBalance());
        assertEquals(100, pointHistory.getAmount());
        assertEquals(100, payment.getAmount());
        assertNull(waitQueueAfter);
    }

    @Test
    void validate_NotEnoughBalance() {
        point.setBalance(50);
        pointRepository.save(point);
        PayTicket.Input input = new PayTicket.Input(userId, ticket.getId());

        assertThrows(NotEnoughBalance.class, () -> { payTicket.execute(input); });
    }

    @Test
    void validate_TicketNotInStatusReserve() {
        ticket.setStatus(TicketStatus.PAID);
        ticketRepository.save(ticket);
        PayTicket.Input input = new PayTicket.Input(userId, ticket.getId());

        assertThrows(TicketNotInStatusReserve.class, () -> { payTicket.execute(input); });
    }

    @Test
    void validate_AlreadyPaidReserve() {
        reserve.setReserveStatus(ReserveStatus.PAID);
        reserveRepository.save(reserve);
        PayTicket.Input input = new PayTicket.Input(userId, ticket.getId());

        assertThrows(AlreadyPaidReserve.class, () -> { payTicket.execute(input); });
    }

    @Test
    void validate_CanceledReserve() {
        reserve.setReserveStatus(ReserveStatus.CANCEL);
        reserveRepository.save(reserve);
        PayTicket.Input input = new PayTicket.Input(userId, ticket.getId());

        assertThrows(CanceledReserve.class, () -> { payTicket.execute(input); });
    }

    @Test
    void validate_ExpiredReserve() {
        reserve.setCreatedAt(LocalDateTime.now().minusMinutes(31));
        reserveRepository.save(reserve);
        PayTicket.Input input = new PayTicket.Input(userId, ticket.getId());

        assertThrows(ExpiredReserve.class, () -> { payTicket.execute(input); });
    }
}
