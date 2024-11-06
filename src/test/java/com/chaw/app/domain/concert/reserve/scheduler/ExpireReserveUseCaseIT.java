package com.chaw.app.domain.concert.reserve.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.ExpireReserveUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ExpireReserveUseCaseIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private ExpireReserveUseCase expireReserveUseCase;

    private Ticket ticket;
    private Reserve reserve;

    @BeforeEach
    void setUp() {
        // Given
        Long userId = 1L;
        ticket = Ticket.builder()
                .status(TicketStatus.RESERVE)
                .reserveUserId(userId)
                .build();
        ticketRepository.save(ticket);

        reserve = Reserve.builder()
                .userId(userId)
                .ticketId(ticket.getId())
                .reserveStatus(ReserveStatus.RESERVE)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .build();
        reserveRepository.save(reserve);
    }

    @Test
    void testExecute() {
        // When
        expireReserveUseCase.execute();

        // Then
        Ticket updatedTicket = ticketRepository.findByIdOrThrow(ticket.getId());
        assertEquals(TicketStatus.EMPTY, updatedTicket.getStatus());
        assertNull(updatedTicket.getReserveUserId());

        Reserve updatedReserve = reserveRepository.findByIdOrThrow(reserve.getId());
        assertEquals(ReserveStatus.CANCEL, updatedReserve.getReserveStatus());
    }
}
