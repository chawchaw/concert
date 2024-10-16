package com.chaw.app.domain.concert.reserve.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.scheduler.ExpireReserve;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
public class ExpireReserveIT {

    @Autowired
    private WaitQueueRepository waitQueueRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private ExpireReserve expireReserve;

    private Ticket ticket;
    private Reserve reserve;
    private WaitQueue waitQueue;

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

        waitQueue = WaitQueue.builder()
                .userId(userId)
                .status(WaitQueueStatus.PASS)
                .build();
        waitQueueRepository.save(waitQueue);
    }

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAll();
        reserveRepository.deleteAll();
        waitQueueRepository.deleteAll();
    }

    @Test
    void testExecute() {
        // When
        expireReserve.execute();

        // Then
        Ticket updatedTicket = ticketRepository.findById(ticket.getId());
        assertEquals(TicketStatus.EMPTY, updatedTicket.getStatus());
        assertNull(updatedTicket.getReserveUserId());

        Reserve updatedReserve = reserveRepository.findById(reserve.getId());
        assertEquals(ReserveStatus.CANCEL, updatedReserve.getReserveStatus());

        assertNull(waitQueueRepository.findByUserId(1L));
    }
}
