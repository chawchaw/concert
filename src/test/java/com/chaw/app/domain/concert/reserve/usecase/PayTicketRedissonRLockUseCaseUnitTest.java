package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketRedissonRLockUseCase;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayTicketRedissonRLockUseCaseUnitTest {

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
    @Mock
    private PaidTicketRepository paidTicketRepository;;

    @InjectMocks
    private PayTicketRedissonRLockUseCase payTicketRedissonRLockUseCase;

    @Test
    void test_예약되지_않은_티켓() {
        // Given
        Point point = Point.builder()
                .userId(1L)
                .build();
        Ticket ticket = Ticket.builder()
                .concertScheduleId(1L)
                .build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .build();

        when(pointRepository.findByUserId(anyLong())).thenReturn(point);
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(concertScheduleRepository.findByIdOrThrow(anyLong())).thenReturn(concertSchedule);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(false);

        // When
        PayTicketRedissonRLockUseCase.Input input = new PayTicketRedissonRLockUseCase.Input(1L, 1L);
        BaseException exception = assertThrows(BaseException.class, () -> payTicketRedissonRLockUseCase.execute(input));

        // Then
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        assertEquals("예약되지 않은 티켓입니다.", exception.getMessage());
    }

    @Test
    void test_이미_결제된_티켓() {
        // Given
        Point point = Point.builder()
                .userId(1L)
                .build();
        Ticket ticket = Ticket.builder()
                .concertScheduleId(1L)
                .build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .build();

        when(pointRepository.findByUserId(anyLong())).thenReturn(point);
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(concertScheduleRepository.findByIdOrThrow(anyLong())).thenReturn(concertSchedule);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(true);
        when(paymentRepository.existsByTicketId(anyLong())).thenReturn(true);

        // When
        PayTicketRedissonRLockUseCase.Input input = new PayTicketRedissonRLockUseCase.Input(1L, 1L);
        BaseException exception = assertThrows(BaseException.class, () -> payTicketRedissonRLockUseCase.execute(input));

        // Then
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        assertEquals("이미 결제된 티켓입니다.", exception.getMessage());
    }

    @Test
    void test_포인트_잔액_부족() {
        // Given
        Point point = Point.builder()
                .userId(1L)
                .balance(1000)
                .build();
        Ticket ticket = Ticket.builder()
                .concertScheduleId(1L)
                .price(2000)
                .build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .build();

        when(pointRepository.findByUserId(anyLong())).thenReturn(point);
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(concertScheduleRepository.findByIdOrThrow(anyLong())).thenReturn(concertSchedule);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(true);
        when(paymentRepository.existsByTicketId(anyLong())).thenReturn(false);

        // When
        PayTicketRedissonRLockUseCase.Input input = new PayTicketRedissonRLockUseCase.Input(1L, 1L);
        BaseException exception = assertThrows(BaseException.class, () -> payTicketRedissonRLockUseCase.execute(input));

        // Then
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        assertEquals("잔액이 부족합니다.", exception.getMessage());
    }

    @Test
    void test_남은_좌석이_없음() {
        // Given
        Point point = Point.builder()
                .userId(1L)
                .balance(1000)
                .build();
        Ticket ticket = Ticket.builder()
                .id(1L)
                .concertScheduleId(1L)
                .price(1000)
                .build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .build();

        when(pointRepository.findByUserId(anyLong())).thenReturn(point);
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(concertScheduleRepository.findByIdOrThrow(anyLong())).thenReturn(concertSchedule);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(true);
        when(paymentRepository.existsByTicketId(anyLong())).thenReturn(false);
        when(concertScheduleRepository.decreaseAvailableSeat(anyLong())).thenReturn(false);

        // When
        PayTicketRedissonRLockUseCase.Input input = new PayTicketRedissonRLockUseCase.Input(1L, 1L);
        BaseException exception = assertThrows(BaseException.class, () -> payTicketRedissonRLockUseCase.execute(input));

        // Then
        assertEquals(ErrorType.DATA_INTEGRITY_VIOLATION, exception.getErrorType());
        assertEquals("남은 좌석이 없습니다. 일정(1), 티켓(1) ", exception.getMessage());
    }

    @Test
    void test_결제_완료() {
        // Given
        Point point = Point.builder()
                .userId(1L)
                .balance(1000)
                .build();
        Ticket ticket = Ticket.builder()
                .id(1L)
                .concertScheduleId(1L)
                .price(1000)
                .build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .build();

        when(pointRepository.findByUserId(anyLong())).thenReturn(point);
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(concertScheduleRepository.findByIdOrThrow(anyLong())).thenReturn(concertSchedule);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(true);
        when(paymentRepository.existsByTicketId(anyLong())).thenReturn(false);
        when(concertScheduleRepository.decreaseAvailableSeat(anyLong())).thenReturn(true);

        // When
        PayTicketRedissonRLockUseCase.Input input = new PayTicketRedissonRLockUseCase.Input(1L, 1L);
        PayTicketRedissonRLockUseCase.Output output = payTicketRedissonRLockUseCase.execute(input);

        // Then
        assertEquals(true, output.success());
        assertEquals(0, output.balance());
        verify(pointRepository, times(1)).save(point);
        verify(pointHistoryRepository, times(1)).save(any());
        verify(paymentRepository, times(1)).save(any());
        verify(paidTicketRepository, times(1)).save(anyLong(), anyLong());
    }
}
