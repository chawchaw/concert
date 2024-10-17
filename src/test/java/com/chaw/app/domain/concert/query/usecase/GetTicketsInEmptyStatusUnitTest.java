package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.entity.TicketType;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class GetTicketsInEmptyStatusUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private GetTicketsInEmptyStatus getTicketsInEmptyStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() {
        // Given
        Long concertScheduleId = 1L;
        GetTicketsInEmptyStatus.Input input = new GetTicketsInEmptyStatus.Input(concertScheduleId);

        Ticket ticket1 = Ticket.builder().id(1L).type(TicketType.VIP).seatNo("A1").price(100).status(TicketStatus.EMPTY).build();
        Ticket ticket2 = Ticket.builder().id(2L).type(TicketType.VIP).seatNo("A2").price(120).status(TicketStatus.EMPTY).build();
        List<Ticket> emptyTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByConcertScheduleIdAndStatus(concertScheduleId, TicketStatus.EMPTY)).thenReturn(emptyTickets);

        // When
        GetTicketsInEmptyStatus.Output output = getTicketsInEmptyStatus.execute(input);

        // Then
        assertEquals(2, output.tickets().size());
        assertEquals("A1", output.tickets().get(0).seatNo());
        assertEquals(100, output.tickets().get(0).price());
        assertEquals("A2", output.tickets().get(1).seatNo());
        assertEquals(120, output.tickets().get(1).price());
    }
}
