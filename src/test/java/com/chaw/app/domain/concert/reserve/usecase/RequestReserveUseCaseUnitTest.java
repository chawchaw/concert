package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserveUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestReserveUseCaseUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @InjectMocks
    private RequestReserveUseCase requestReserveUseCase;

    @Test
    void testExecute_Success() {
        // Given
        Long ticketId = 1L;
        Long userId = 1L;

        Concert concert = Concert.builder()
                .id(1L)
                .name("concert")
                .build();

        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .availableSeat(10)
                .build();

        Ticket ticket = spy(
                Ticket.builder()
                        .id(ticketId)
                        .status(TicketStatus.EMPTY)
                        .price(100)
                        .build()
        );

        when(ticketRepository.findByIdWithLockOrThrow(ticketId)).thenReturn(ticket);

        // When
        RequestReserveUseCase.Input input = new RequestReserveUseCase.Input(userId, ticketId);
        RequestReserveUseCase.Output output = requestReserveUseCase.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(true, output.success());

        verify(ticket, times(1)).isReservableOrThrow();
        verify(ticketRepository, times(1)).findByIdWithLockOrThrow(ticketId);
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(any(Reserve.class));
    }
}
