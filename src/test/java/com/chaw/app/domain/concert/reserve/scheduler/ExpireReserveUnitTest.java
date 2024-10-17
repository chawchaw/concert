package com.chaw.app.domain.concert.reserve.scheduler;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.scheduler.ExpireReserve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ExpireReserveUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ReserveRepository reserveRepository;

    @InjectMocks
    private ExpireReserve expireReserve;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        Field field = ExpireReserve.class.getDeclaredField("EXPIRED_MINUTES");
        field.setAccessible(true);
        field.set(expireReserve, 10);
    }

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

        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).reserveUserId(1L).build();
        when(ticketRepository.findById(1L)).thenReturn(ticket);

        WaitQueue waitQueue = WaitQueue.builder().userId(1L).build();
        when(waitQueueRepository.findByUserId(1L)).thenReturn(waitQueue);

        // When
        expireReserve.execute();

        // Then
        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(expiredReserve);
        verify(waitQueueRepository, times(1)).delete(waitQueue);
        assertEquals(TicketStatus.EMPTY, ticket.getStatus());
        assertEquals(ReserveStatus.CANCEL, expiredReserve.getReserveStatus());
    }

    @Test
    void testCancelReserve() {
        // Given
        Reserve reserve = Reserve.builder().id(1L).userId(1L).ticketId(1L).reserveStatus(ReserveStatus.RESERVE).build();
        Ticket ticket = Ticket.builder().id(1L).status(TicketStatus.RESERVE).reserveUserId(1L).build();
        when(ticketRepository.findById(1L)).thenReturn(ticket);

        WaitQueue waitQueue = WaitQueue.builder().userId(1L).build();
        when(waitQueueRepository.findByUserId(1L)).thenReturn(waitQueue);

        // When
        expireReserve.cancelReserve(reserve);

        // Then
        verify(ticketRepository, times(1)).save(ticket);
        verify(reserveRepository, times(1)).save(reserve);
        verify(waitQueueRepository, times(1)).delete(waitQueue);

        assertEquals(TicketStatus.EMPTY, ticket.getStatus());
        assertEquals(ReserveStatus.CANCEL, reserve.getReserveStatus());
    }
}
