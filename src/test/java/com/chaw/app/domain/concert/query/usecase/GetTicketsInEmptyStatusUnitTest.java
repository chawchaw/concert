package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
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

        Ticket ticket1 = new Ticket();
        Ticket ticket2 = new Ticket();
        List<Ticket> emptyTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByConcertScheduleIdAndStatus(concertScheduleId, TicketStatus.EMPTY)).thenReturn(emptyTickets);

        // When
        GetTicketsInEmptyStatus.Output output = getTicketsInEmptyStatus.execute(input);

        // Then
        assertEquals(emptyTickets, output.getTickets());
    }
}
