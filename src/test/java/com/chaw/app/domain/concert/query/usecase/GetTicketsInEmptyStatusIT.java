package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.entity.TicketType;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        Ticket ticket1 = Ticket.builder().concertScheduleId(1L).type(TicketType.VIP).status(TicketStatus.EMPTY).seatNo("A1").price(100).build();
        Ticket ticket2 = Ticket.builder().concertScheduleId(1L).type(TicketType.VIP).status(TicketStatus.EMPTY).seatNo("A2").price(120).build();

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
        assertNotNull(output);
        assertEquals(2, output.tickets().size());

        GetTicketsInEmptyStatus.Output.Item firstTicket = output.tickets().get(0);
        GetTicketsInEmptyStatus.Output.Item secondTicket = output.tickets().get(1);

        assertEquals("A1", firstTicket.seatNo());
        assertEquals(100, firstTicket.price());
        assertEquals("A2", secondTicket.seatNo());
        assertEquals(120, secondTicket.price());
    }
}
