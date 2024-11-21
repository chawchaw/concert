package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReservedEventRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.ReserveUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.dto.ReservedEvent;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReserveUseCaseUnitTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ReserveRepository reserveRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ReservedEventRepository reservedEventRepository;
    @InjectMocks
    private ReserveUseCase reserveUseCase;

    @Test
    void test_예약된_티켓() {
        // Given
        Ticket ticket = Ticket.builder()
                .concertScheduleId(1L)
                .build();
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(true);

        // When
        ReserveUseCase.Input input = new ReserveUseCase.Input(1L, 1L);
        BaseException exception = assertThrows(BaseException.class, () -> reserveUseCase.execute(input));

        // Then
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        assertEquals("이미 예약된 티켓입니다.", exception.getMessage());
    }

    @Test
    void test_결제된_티켓() {
        // Given
        Ticket ticket = Ticket.builder()
                .concertScheduleId(1L)
                .build();
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(false);
        when(paymentRepository.existsByTicketId(anyLong())).thenReturn(true);

        // When
        ReserveUseCase.Input input = new ReserveUseCase.Input(1L, 1L);
        BaseException exception = assertThrows(BaseException.class, () -> reserveUseCase.execute(input));

        // Then
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
        assertEquals("이미 결제된 티켓입니다.", exception.getMessage());
    }

    @Test
    void test_예약_완료() {
        // Given
        Ticket ticket = Ticket.builder()
                .concertScheduleId(1L)
                .build();
        when(ticketRepository.findByIdOrThrow(anyLong())).thenReturn(ticket);
        when(reserveRepository.existsByConcertScheduleIdAndTicketIdAndUserId(anyLong(), anyLong(), anyLong())).thenReturn(false);
        when(paymentRepository.existsByTicketId(anyLong())).thenReturn(false);
        doNothing().when(reserveRepository).save(any());

        ReservedEvent reservedEvent = new ReservedEvent(1L, 1L, 1L);
        doNothing().when(reservedEventRepository).complete(reservedEvent);

        // When
        ReserveUseCase.Input input = new ReserveUseCase.Input(1L, 1L);
        ReserveUseCase.Output output = reserveUseCase.execute(input);

        // Then
        assertEquals(true, output.success());
        verify(reserveRepository, times(1)).save(any());
        verify(reservedEventRepository, times(1)).complete(reservedEvent);
    }
}
