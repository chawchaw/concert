package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class RequestReserveIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private RequestReserve requestReserve;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = Ticket.builder()
                .status(TicketStatus.EMPTY)
                .price(100)
                .build();
        ticketRepository.save(ticket);
    }

    @Test
    void testExecute_Success() {
        // Given
        Long ticketId = ticket.getId();
        Long userId = 1L;

        RequestReserve.Input input = new RequestReserve.Input(userId, ticketId);

        // When
        RequestReserve.Output output = requestReserve.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(TicketStatus.RESERVE, output.ticket().getStatus());

        Ticket updatedTicket = ticketRepository.findById(ticketId);
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());
        assertEquals(userId, updatedTicket.getReserveUserId());

        Reserve reserve = reserveRepository.findByTicketId(ticketId);
        assertEquals(ReserveStatus.RESERVE, reserve.getReserveStatus());
        assertEquals(ticketId, reserve.getTicketId());
        assertEquals(userId, reserve.getUserId());
    }
}
