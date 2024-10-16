package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReserved;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class RequestReserveIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RequestReserve requestReserve;

    @Test
    void testRequestReserveThrowTicketNotFound() {
        // Given
        RequestReserve.Input input = new RequestReserve.Input(1L, 1L);

        // When & Then
        assertThrows(TicketNotFound.class, () -> requestReserve.execute(input));
    }

    @Test
    void testRequestReserveThrowTicketAlreadyReserved() {
        // Given
        Ticket ticket = Ticket.builder().status(TicketStatus.RESERVE).build();
        ticketRepository.save(ticket);
        RequestReserve.Input input = new RequestReserve.Input(1L, ticket.getId());

        // When & Then
        assertThrows(TicketAlreadyReserved.class, () -> requestReserve.execute(input));
    }

    @Test
    void testRequestReserve() {
        // Given
        Long userId = 1L;
        Ticket ticket = Ticket.builder().status(TicketStatus.EMPTY).build();
        ticketRepository.save(ticket);
        RequestReserve.Input input = new RequestReserve.Input(userId, ticket.getId());

        // When
        RequestReserve.Output output = requestReserve.execute(input);

        // Then
        Ticket reservedTicket = output.ticket();
        Ticket foundTicket = ticketRepository.findById(reservedTicket.getId()).get();
        assertEquals(reservedTicket, foundTicket);
        assertEquals(TicketStatus.RESERVE, foundTicket.getStatus());
        assertEquals(userId, foundTicket.getReserveUserId());
    }
}
