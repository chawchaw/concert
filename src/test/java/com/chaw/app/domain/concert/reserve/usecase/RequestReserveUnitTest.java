package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReservedException;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFoundException;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RequestReserveUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @InjectMocks
    private RequestReserve requestReserve;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute_TicketNotFound() {
        // Given
        Long ticketId = 1L;
        Long userId = 1L;
        when(ticketRepository.findByIdWithLock(ticketId)).thenReturn(null);

        // When / Then
        RequestReserve.Input input = new RequestReserve.Input(userId, ticketId);
        assertThrows(TicketNotFoundException.class, () -> requestReserve.execute(input));

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(reserveRepository, never()).save(any());
    }

    @Test
    void testExecute_TicketAlreadyReserved() {
        // Given
        Long ticketId = 1L;
        Long userId = 1L;
        Ticket ticket = Ticket.builder().id(ticketId).status(TicketStatus.RESERVE).build();
        when(ticketRepository.findByIdWithLock(ticketId)).thenReturn(ticket);

        // When / Then
        RequestReserve.Input input = new RequestReserve.Input(userId, ticketId);
        assertThrows(TicketAlreadyReservedException.class, () -> requestReserve.execute(input));

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(reserveRepository, never()).save(any());
    }

    @Test
    void testExecute_Success() {
        // Given
        Long ticketId = 1L;
        Long userId = 1L;
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .status(TicketStatus.EMPTY)
                .price(100)
                .build();
        when(ticketRepository.findByIdWithLock(ticketId)).thenReturn(ticket);

        // When
        RequestReserve.Input input = new RequestReserve.Input(userId, ticketId);
        RequestReserve.Output output = requestReserve.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(TicketStatus.RESERVE, output.ticket().getStatus());

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(any(Reserve.class));
    }
}
