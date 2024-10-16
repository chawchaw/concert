package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReserved;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class RequestReserveUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private RequestReserve requestReserve;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() {
        // Given
        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.EMPTY).build();

        when(ticketRepository.findByIdWithLock(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // When
        RequestReserve.Output output = requestReserve.execute(new RequestReserve.Input(1L, 1L));

        // Then
        assertEquals(ticket, output.ticket());
    }

    @Test
    void testExecuteWhenTicketNotFound() {
        // Given
        when(ticketRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TicketNotFound.class, () -> requestReserve.execute(new RequestReserve.Input(1L, 1L)));
    }

    @Test
    void testExecuteWhenTicketAlreadyReserved() {
        // Given
        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).build();

        when(ticketRepository.findByIdWithLock(1L)).thenReturn(Optional.of(ticket));

        // When & Then
        assertThrows(TicketAlreadyReserved.class, () -> requestReserve.execute(new RequestReserve.Input(1L, 1L)));
    }
}
