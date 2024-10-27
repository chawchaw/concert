package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class RequestReserveIT {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private RequestReserve requestReserve;

    private Concert concert;
    private ConcertSchedule concertSchedule;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        concert = Concert.builder()
                .name("concert")
                .build();
        concertRepository.save(concert);

        concertSchedule = ConcertSchedule.builder()
                .concertId(concert.getId())
                .isSoldOut(false)
                .totalSeat(10)
                .availableSeat(10)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule);

        ticket = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
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

        RequestReserve.Input input = new RequestReserve.Input(userId, concert.getId(), concertSchedule.getId(), ticketId);

        // When
        RequestReserve.Output output = requestReserve.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(true, output.success());

        Ticket updatedTicket = ticketRepository.findById(ticketId);
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());
        assertEquals(userId, updatedTicket.getReserveUserId());

        Reserve reserve = reserveRepository.findByTicketId(ticketId);
        assertEquals(ReserveStatus.RESERVE, reserve.getReserveStatus());
        assertEquals(ticketId, reserve.getTicketId());
        assertEquals(userId, reserve.getUserId());
    }
}
