package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReservedException;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFoundException;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
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
    private ConcertRepository concertRepository;

    @Mock
    private ConcertScheduleRepository concertScheduleRepository;

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

        Concert concert = Concert.builder()
                .id(1L)
                .name("concert")
                .build();

        ConcertSchedule concertSchedule = ConcertSchedule.builder()
                .id(1L)
                .availableSeat(10)
                .build();

        when(concertRepository.findById(1L)).thenReturn(concert);
        when(concertScheduleRepository.findById(1L)).thenReturn(concertSchedule);
        when(ticketRepository.findByIdWithLock(ticketId)).thenReturn(null);

        // When / Then
        RequestReserve.Input input = new RequestReserve.Input(userId, 1L, 1L, ticketId);
        assertThrows(TicketNotFoundException.class, () -> requestReserve.execute(input));

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(reserveRepository, never()).save(any());
    }

    @Test
    void testExecute_TicketAlreadyReserved() {
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

        Ticket ticket = Ticket.builder().id(ticketId).status(TicketStatus.RESERVE).build();

        when(concertRepository.findById(1L)).thenReturn(concert);
        when(concertScheduleRepository.findById(1L)).thenReturn(concertSchedule);
        when(ticketRepository.findByIdWithLock(ticketId)).thenReturn(ticket);

        // When / Then
        RequestReserve.Input input = new RequestReserve.Input(userId, 1L, 1L, ticketId);
        assertThrows(TicketAlreadyReservedException.class, () -> requestReserve.execute(input));

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(reserveRepository, never()).save(any());
    }

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

        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .status(TicketStatus.EMPTY)
                .price(100)
                .build();

        when(concertRepository.findById(1L)).thenReturn(concert);
        when(concertScheduleRepository.findById(1L)).thenReturn(concertSchedule);
        when(ticketRepository.findByIdWithLock(ticketId)).thenReturn(ticket);

        // When
        RequestReserve.Input input = new RequestReserve.Input(userId, 1L, 1L, ticketId);
        RequestReserve.Output output = requestReserve.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(TicketStatus.RESERVE, output.ticket().getStatus());

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(any(Reserve.class));
    }
}
