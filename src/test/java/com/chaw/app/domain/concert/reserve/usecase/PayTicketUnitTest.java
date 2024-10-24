package com.chaw.app.domain.concert.reserve.usecase;

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
import com.chaw.concert.app.domain.concert.reserve.entity.Payment;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.exception.AvailableSeatNotExistException;
import com.chaw.concert.app.domain.concert.reserve.exception.ExpiredReserveException;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
import com.chaw.concert.app.domain.concert.reserve.validation.ReserveValidation;
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
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReserveValidation reserveValidation;

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

        Concert concert = Concert.builder()
                .id(1L)
                .name("concert")
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

        when(pointRepository.findByUserIdWithLock(userId)).thenReturn(point);
        when(ticketRepository.findById(ticketId)).thenReturn(ticket);
        when(concertRepository.findById(ticket.getConcertScheduleId())).thenReturn(concert);
        when(concertScheduleRepository.findByIdWithLock(ticket.getConcertScheduleId())).thenReturn(concertSchedule);
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimit(userId, ticketId, 1)).thenReturn(reserve);
        doNothing().when(reserveValidation).validateConcertDetails(concert, concertSchedule, ticket);
        doNothing().when(reserveValidation).validatePayTicketDetails(point, reserve, ticket);
        when(concertScheduleRepository.decreaseAvailableSeat(concertSchedule.getId())).thenReturn(true);

        PayTicket.Input input = new PayTicket.Input(userId, 1L, 1L, ticketId);

        // when
        PayTicket.Output output = payTicket.execute(input);

        // then
        assertEquals(500, output.balance()); // 남은 포인트 확인

        verify(reserveValidation, times(1)).validateConcertDetails(concert, concertSchedule, ticket);
        verify(reserveValidation, times(1)).validatePayTicketDetails(point, reserve, ticket);

        verify(concertScheduleRepository).decreaseAvailableSeat(anyLong());
        verify(ticketRepository).save(any(Ticket.class));
        verify(reserveRepository).save(any(Reserve.class));
        verify(pointRepository).save(any(Point.class));
        verify(pointHistoryRepository).save(any(PointHistory.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    public void testExecute_AvailableSeatNotExist() {
        // given
        Long userId = 1L;
        Long concertScheduleId = 1L;
        Long ticketId = 1L;
        Reserve reserve = Reserve.builder()
                .id(1L)
                .userId(userId)
                .ticketId(ticketId)
                .amount(500)
                .reserveStatus(ReserveStatus.RESERVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(pointRepository.findByUserIdWithLock(anyLong())).thenReturn(Point.builder().build());
        when(ticketRepository.findById(anyLong())).thenReturn(Ticket.builder().concertScheduleId(concertScheduleId).build());
        when(concertRepository.findById(anyLong())).thenReturn(Concert.builder().build());
        when(concertScheduleRepository.findByIdWithLock(anyLong())).thenReturn(ConcertSchedule.builder().id(concertScheduleId).build());
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimit(anyLong(), anyLong(), anyInt())).thenReturn(reserve);

        doNothing().when(reserveValidation).validateConcertDetails(any(), any(), any());
        doNothing().when(reserveValidation).validatePayTicketDetails(any(), any(), any());

        when(concertScheduleRepository.decreaseAvailableSeat(anyLong())).thenReturn(false);

        // when / then
        assertThrows(AvailableSeatNotExistException.class, () -> payTicket.execute(new PayTicket.Input(1L, 1L, 1L, 1L)));
    }

    @Test
    void testHandleExpiredReserve_Expired() {
        // Given
        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).build();
        Reserve reserve = Reserve.builder().amount(500).createdAt(LocalDateTime.now().minusMinutes(60)).reserveStatus(ReserveStatus.RESERVE).build(); // 만료된 상태

        // Mocking
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(reserveRepository.save(reserve)).thenReturn(reserve);

        // When / Then
        assertThrows(ExpiredReserveException.class, () -> {
            payTicket.handleExpiredReserve(ticket, reserve);
        });

        // Verify
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(reserve);
    }

    @Test
    void testHandleExpiredReserve_NotExpired() {
        // Given
        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).build();
        Reserve reserve = Reserve.builder().amount(500).createdAt(LocalDateTime.now()).reserveStatus(ReserveStatus.RESERVE).build(); // 만료되지 않은 상태

        // When
        payTicket.handleExpiredReserve(ticket, reserve);

        // Verify (아무 작업도 발생하지 않음)
        verify(ticketRepository, never()).save(any());
        verify(reserveRepository, never()).save(any());
    }

    @Test
    void testExecute_ExpiredReserve() {
        // Given
        Long userId = 1L;
        Long concertId = 1L;
        Long ticketId = 1L;

        Point point = Point.builder().balance(1000).build();
        Concert concert = Concert.builder().id(concertId).build();
        Ticket ticket = Ticket.builder().id(ticketId).concertScheduleId(1L).status(TicketStatus.RESERVE).build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder().id(1L).availableSeat(10).build();
        Reserve reserve = Reserve.builder().amount(500).createdAt(LocalDateTime.now().minusMinutes(60)).reserveStatus(ReserveStatus.RESERVE).build(); // 만료된 상태

        // Mocking
        when(pointRepository.findByUserIdWithLock(userId)).thenReturn(point);
        when(concertRepository.findById(concertId)).thenReturn(concert);
        when(ticketRepository.findById(ticketId)).thenReturn(ticket);
        when(concertScheduleRepository.findByIdWithLock(1L)).thenReturn(concertSchedule);
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimit(userId, ticketId, 1)).thenReturn(reserve);

        // When / Then
        PayTicket.Input input = new PayTicket.Input(userId, concertId, 1L, ticketId);
        assertThrows(ExpiredReserveException.class, () -> payTicket.execute(input));

        // Verify
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(reserve);
    }
}
