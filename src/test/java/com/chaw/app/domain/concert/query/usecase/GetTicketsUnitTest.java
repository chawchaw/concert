package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketType;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsUseCase;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTicketsUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @Mock
    private PaidTicketRepository paidTicketRepository;

    @InjectMocks
    private GetTicketsUseCase getTicketsUseCase;

    @Test
    void test_모두조회() {
        // Given
        Long concertScheduleId = 1L;

        GetTicketsUseCase.Input input = new GetTicketsUseCase.Input(concertScheduleId);

        Ticket ticket1 = Ticket.builder().id(1L).type(TicketType.VIP).seatNo("A1").price(100).build();
        Ticket ticket2 = Ticket.builder().id(2L).type(TicketType.VIP).seatNo("A2").price(120).build();
        List<Ticket> emptyTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByConcertScheduleIdWithCache(concertScheduleId)).thenReturn(emptyTickets);
        when(reserveRepository.findByConcertScheduleId(concertScheduleId)).thenReturn(Set.of());

        // When
        GetTicketsUseCase.Output output = getTicketsUseCase.execute(input);

        // Then
        verify(ticketRepository, times(1)).findByConcertScheduleIdWithCache(concertScheduleId);
        verify(reserveRepository, times(1)).findByConcertScheduleId(concertScheduleId);
        verify(paidTicketRepository, times(1)).findByConcertScheduleId(concertScheduleId);
        assertEquals(2, output.tickets().size());
        assertEquals("A1", output.tickets().get(0).seatNo());
        assertEquals(100, output.tickets().get(0).price());
        assertEquals("A2", output.tickets().get(1).seatNo());
        assertEquals(120, output.tickets().get(1).price());
    }

    @Test
    void test_예약된티켓이있을때() {
        // Given
        Long concertScheduleId = 1L;

        GetTicketsUseCase.Input input = new GetTicketsUseCase.Input(concertScheduleId);

        Ticket ticket1 = Ticket.builder().id(1L).type(TicketType.VIP).seatNo("A1").price(100).build();
        Ticket ticket2 = Ticket.builder().id(2L).type(TicketType.VIP).seatNo("A2").price(120).build();
        List<Ticket> emptyTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByConcertScheduleIdWithCache(concertScheduleId)).thenReturn(emptyTickets);
        when(reserveRepository.findByConcertScheduleId(concertScheduleId)).thenReturn(Set.of(2L));

        // When
        GetTicketsUseCase.Output output = getTicketsUseCase.execute(input);

        // Then
        verify(ticketRepository, times(1)).findByConcertScheduleIdWithCache(concertScheduleId);
        verify(reserveRepository, times(1)).findByConcertScheduleId(concertScheduleId);
        verify(paidTicketRepository, times(1)).findByConcertScheduleId(concertScheduleId);
        assertEquals(1, output.tickets().size());
    }

    @Test
    void test_결제된티켓이있을때() {
        // Given
        Long concertScheduleId = 1L;
        Long ticketId1 = 1L;
        Long ticketId2 = 2L;

        GetTicketsUseCase.Input input = new GetTicketsUseCase.Input(concertScheduleId);

        Ticket ticket1 = Ticket.builder().id(ticketId1).type(TicketType.VIP).seatNo("A1").price(100).build();
        Ticket ticket2 = Ticket.builder().id(ticketId2).type(TicketType.VIP).seatNo("A2").price(120).build();
        List<Ticket> emptyTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByConcertScheduleIdWithCache(concertScheduleId)).thenReturn(emptyTickets);
        when(paidTicketRepository.findByConcertScheduleId(concertScheduleId)).thenReturn(Set.of(1L, 2L));

        // When
        GetTicketsUseCase.Output output = getTicketsUseCase.execute(input);

        // Then
        verify(ticketRepository, times(1)).findByConcertScheduleIdWithCache(concertScheduleId);
        verify(reserveRepository, times(1)).findByConcertScheduleId(concertScheduleId);
        verify(paidTicketRepository, times(1)).findByConcertScheduleId(concertScheduleId);
        assertEquals(0, output.tickets().size());
    }

}
