package com.chaw.app.domain.concert.reserve.scheduler;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.ExpireReserveUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpireReserveUseCaseUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @InjectMocks
    private ExpireReserveUseCase expireReserveUseCase;

    @Test
    void testExecute_ExpireReservesFound() {
        // Given
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
        Reserve expiredReserve = Reserve.builder()
                .id(1L)
                .userId(1L)
                .ticketId(1L)
                .reserveStatus(ReserveStatus.RESERVE)
                .createdAt(expiredTime.minusMinutes(5))
                .build();
        List<Reserve> expiredReserves = Arrays.asList(expiredReserve);

        when(reserveRepository.findByReserveStatusAndCreatedAtBefore(any(), any())).thenReturn(expiredReserves);

        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).build();
        when(ticketRepository.findByIdOrThrow(1L)).thenReturn(ticket);

        // When
        expireReserveUseCase.execute();

        // Then
        verify(ticketRepository, times(1)).findByIdOrThrow(1L);
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(expiredReserve);
        assertEquals(TicketStatus.EMPTY, ticket.getStatus());
        assertEquals(ReserveStatus.CANCEL, expiredReserve.getReserveStatus());
    }

    @Test
    void testCancelReserve() {
        // Given
        Reserve reserve = Reserve.builder().id(1L).userId(1L).ticketId(1L).reserveStatus(ReserveStatus.RESERVE).build();
        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).build();
        when(ticketRepository.findByIdOrThrow(1L)).thenReturn(ticket);

        // When
        expireReserveUseCase.cancelReserve(reserve);

        // Then
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(reserve);

        assertEquals(TicketStatus.EMPTY, ticket.getStatus());
        assertEquals(ReserveStatus.CANCEL, reserve.getReserveStatus());
    }
}
