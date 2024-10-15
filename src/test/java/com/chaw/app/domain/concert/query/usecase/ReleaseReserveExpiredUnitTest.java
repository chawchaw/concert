package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.ReleaseReserveExpired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class ReleaseReserveExpiredUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ReleaseReserveExpired releaseReserveExpired;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute() {
        // Given
        Ticket ticket1 = new Ticket();
        ticket1.setReserveUserId(1L);
        ticket1.setReserveEndAt(null);

        Ticket ticket2 = new Ticket();
        ticket2.setReserveUserId(2L);
        ticket2.setReserveEndAt(null);

        List<Ticket> expiredTickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findByReserveExpired()).thenReturn(expiredTickets);

        // When
        releaseReserveExpired.execute();

        // Then
        verify(ticketRepository, times(1)).findByReserveExpired();
        verify(ticketRepository, times(2)).save(any(Ticket.class));

        // 검증: 예약이 해제되었는지 확인
        for (Ticket ticket : expiredTickets) {
            assertNull(ticket.getReserveUserId());
            assertNull(ticket.getReserveEndAt());
        }
    }
}
