package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class GetTicketsInEmptyStatusIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private GetTicketsInEmptyStatus getTicketsInEmptyStatus;

    @BeforeEach
    void setUp() {
        Ticket ticket1 = Ticket.builder().concertScheduleId(1L).status(TicketStatus.EMPTY).build();
        Ticket ticket2 = Ticket.builder().concertScheduleId(1L).status(TicketStatus.EMPTY).build();

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
    }

    @Test
    void testGetTicketsInEmptyStatus() {
        // Given
        Long concertScheduleId = 1L;
        GetTicketsInEmptyStatus.Input input = new GetTicketsInEmptyStatus.Input(concertScheduleId);

        // When
        GetTicketsInEmptyStatus.Output output = getTicketsInEmptyStatus.execute(input);

        // Then
        List<Ticket> tickets = output.getTickets();
        assertEquals(2, tickets.size());
        for (Ticket ticket : tickets) {
            assertEquals(TicketStatus.EMPTY, ticket.getStatus());
        }
    }
}
