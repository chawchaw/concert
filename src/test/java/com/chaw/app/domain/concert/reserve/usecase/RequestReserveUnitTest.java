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
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import com.chaw.concert.app.domain.concert.reserve.validation.ReserveValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Mock
    private ReserveValidation reserveValidation;

    @InjectMocks
    private RequestReserve requestReserve;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        assertEquals(true, output.success());

        verify(ticketRepository, times(1)).findByIdWithLock(ticketId);
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveValidation, times(1)).validateConcertDetails(userId, concert, concertSchedule, ticket);
        verify(reserveValidation, times(1)).validateReserveDetails(ticket);
        verify(reserveRepository, times(1)).save(any(Reserve.class));
    }
}
