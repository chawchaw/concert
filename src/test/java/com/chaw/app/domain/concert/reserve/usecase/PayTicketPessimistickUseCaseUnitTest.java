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
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketPessimistickUseCase;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PayTicketPessimistickUseCaseUnitTest {

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

    @InjectMocks
    private PayTicketPessimistickUseCase payTicketPessimistickUseCase;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
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
        when(ticketRepository.findByIdOrThrow(ticketId)).thenReturn(ticket);
        when(concertRepository.findByIdOrThrow(ticket.getConcertScheduleId())).thenReturn(concert);
        when(concertScheduleRepository.findByIdWithLockThrow(ticket.getConcertScheduleId())).thenReturn(concertSchedule);
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(userId, ticketId, 1)).thenReturn(reserve);
        when(concertScheduleRepository.decreaseAvailableSeat(concertSchedule.getId())).thenReturn(true);

        PayTicketPessimistickUseCase.Input input = new PayTicketPessimistickUseCase.Input(userId, ticket.getId());

        // when
        PayTicketPessimistickUseCase.Output output = payTicketPessimistickUseCase.execute(input);

        // then
        assertEquals(500, output.balance()); // 남은 포인트 확인

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

        when(pointRepository.findByUserIdWithLock(anyLong())).thenReturn(Point.builder().balance(1000).build());
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(Ticket.builder().status(TicketStatus.RESERVE).concertScheduleId(concertScheduleId).build());
        when(concertRepository.findByIdOrThrow(anyLong())).thenReturn(Concert.builder().build());
        when(concertScheduleRepository.findByIdWithLockThrow(anyLong())).thenReturn(ConcertSchedule.builder().id(concertScheduleId).build());
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(anyLong(), anyLong(), anyInt())).thenReturn(reserve);

        when(concertScheduleRepository.decreaseAvailableSeat(anyLong())).thenReturn(false);

        // when / then
        BaseException baseException = assertThrows(BaseException.class, () -> payTicketPessimistickUseCase.execute(new PayTicketPessimistickUseCase.Input(1L, 1L)));
        assertEquals(ErrorType.DATA_INTEGRITY_VIOLATION, baseException.getErrorType());
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
        when(concertRepository.findByIdOrThrow(concertId)).thenReturn(concert);
        when(ticketRepository.findByIdOrThrow(ticketId)).thenReturn(ticket);
        when(concertScheduleRepository.findByIdWithLockThrow(1L)).thenReturn(concertSchedule);
        when(reserveRepository.findByUserIdAndTicketIdOrderByIdDescLimitOrThrow(userId, ticketId, 1)).thenReturn(reserve);

        // When / Then
        PayTicketPessimistickUseCase.Input input = new PayTicketPessimistickUseCase.Input(userId, ticketId);
        BaseException exception = assertThrows(BaseException.class, () -> payTicketPessimistickUseCase.execute(input));

        // Verify
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(reserve);
    }
}
