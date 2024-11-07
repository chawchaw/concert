package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.*;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatusUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTicketsInEmptyReserveStatusUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private GetTicketsInEmptyStatusUseCase getTicketsInEmptyStatusUseCase;

    @Test
    void testExecute() {
        // Given
        Long concertScheduleId = 1L;

        GetTicketsInEmptyStatusUseCase.Input input = new GetTicketsInEmptyStatusUseCase.Input(concertScheduleId);

        Ticket ticket1 = Ticket.builder().id(1L).type(TicketType.VIP).seatNo("A1").price(100).build();
        Ticket ticket2 = Ticket.builder().id(2L).type(TicketType.VIP).seatNo("A2").price(120).build();
        List<Ticket> emptyTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByConcertScheduleId(concertScheduleId)).thenReturn(emptyTickets);

        // When
        GetTicketsInEmptyStatusUseCase.Output output = getTicketsInEmptyStatusUseCase.execute(input);

        // Then
        assertEquals(2, output.tickets().size());
        assertEquals("A1", output.tickets().get(0).seatNo());
        assertEquals(100, output.tickets().get(0).price());
        assertEquals("A2", output.tickets().get(1).seatNo());
        assertEquals(120, output.tickets().get(1).price());
    }
}
