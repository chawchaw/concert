package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.exception.NotEnoughBalanceException;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PayTicketUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PayTicket payTicket;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        Field field = PayTicket.class.getDeclaredField("EXPIRED_MINUTES");
        field.setAccessible(true);
        field.set(payTicket, 10);
    }

    @Test
    public void testSuccessfulPayment() {
        // given
        Long userId = 1L;
        Long ticketId = 1L;

        Point point = Point.builder()
                .id(1L)
                .userId(userId)
                .balance(1000) // 1000 포인트 보유
                .build();

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .status(TicketStatus.RESERVE)
                .concertScheduleId(1L)
                .build();

        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .availableSeat(10)
                .build();

        Reserve reserve = Reserve.builder()
                .id(1L)
                .userId(userId)
                .ticketId(ticketId)
                .amount(500)
                .reserveStatus(ReserveStatus.RESERVE)
                .createdAt(LocalDateTime.now())
                .build();

        WaitQueue waitQueue = WaitQueue.builder()
                .id(1L)
                .userId(userId)
                .build();

        when(pointRepository.findByUserIdWithLock(userId)).thenReturn(point);
        when(ticketRepository.findById(ticketId)).thenReturn(ticket);
        when(concertScheduleRepository.findByIdWithLock(ticket.getConcertScheduleId())).thenReturn(concertSchedule);
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimit(userId, ticketId, 1)).thenReturn(reserve);
        when(waitQueueRepository.findByUserId(userId)).thenReturn(waitQueue);

        PayTicket.Input input = new PayTicket.Input(userId, ticketId);

        // when
        PayTicket.Output output = payTicket.execute(input);

        // then
        assertEquals(500, output.balance()); // 남은 포인트 확인

        verify(concertScheduleRepository).save(any(ConcertSchedule.class));
        verify(ticketRepository).save(any(Ticket.class));
        verify(reserveRepository).save(any(Reserve.class));
        verify(pointRepository).save(any(Point.class));
        verify(pointHistoryRepository).save(any(PointHistory.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(waitQueueRepository).delete(waitQueue);
    }

    @Test
    public void testNotEnoughBalance() {
        // given
        Long userId = 1L;
        Long ticketId = 1L;

        Point point = Point.builder()
                .id(1L)
                .userId(userId)
                .balance(400) // 잔액 부족
                .build();

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .status(TicketStatus.RESERVE)
                .concertScheduleId(1L)
                .build();

        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .availableSeat(10)
                .build();

        Reserve reserve = Reserve.builder()
                .id(1L)
                .userId(userId)
                .ticketId(ticketId)
                .amount(500)
                .reserveStatus(ReserveStatus.RESERVE)
                .createdAt(LocalDateTime.now())
                .build();

        WaitQueue waitQueue = WaitQueue.builder()
                .id(1L)
                .userId(userId)
                .build();

        when(pointRepository.findByUserIdWithLock(userId)).thenReturn(point);
        when(ticketRepository.findById(ticketId)).thenReturn(ticket);
        when(concertScheduleRepository.findByIdWithLock(ticket.getConcertScheduleId())).thenReturn(concertSchedule);
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimit(userId, ticketId, 1)).thenReturn(reserve);
        when(waitQueueRepository.findByUserId(userId)).thenReturn(waitQueue);

        PayTicket.Input input = new PayTicket.Input(userId, ticketId);

        // when & then
        assertThrows(NotEnoughBalanceException.class, () -> payTicket.execute(input));

        verify(waitQueueRepository, never()).delete(any()); // 잔액 부족 시 대기열 삭제되지 않음
    }
}
