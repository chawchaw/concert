package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketType;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.GetTicketsInEmptyStatusUseCase;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class GetTicketsInEmptyReserveStatusIT {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private PaidTicketRepository paidTicketRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GetTicketsInEmptyStatusUseCase getTicketsInEmptyStatusUseCase;

    private Concert concert;
    private ConcertSchedule concertSchedule;

    @BeforeEach
    void setUp() {
        concert = Concert.builder()
                .name("concert")
                .build();
        concertRepository.save(concert);

        concertSchedule = ConcertSchedule.builder()
                .concertId(1L)
                .isSoldOut(false)
                .totalSeat(10)
                .availableSeat(10)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule);

        Ticket ticket1 = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
                .type(TicketType.VIP)
                .seatNo("A1")
                .price(100)
                .build();
        Ticket ticket2 = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
                .type(TicketType.VIP)
                .seatNo("A2")
                .price(120)
                .build();

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
    }

    @Test
    void testGetTicketsInEmptyStatus() {
        // Given
        GetTicketsInEmptyStatusUseCase.Input input = new GetTicketsInEmptyStatusUseCase.Input(concertSchedule.getId());

        // When
        GetTicketsInEmptyStatusUseCase.Output output = getTicketsInEmptyStatusUseCase.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(2, output.tickets().size());

        GetTicketsInEmptyStatusUseCase.Output.Item firstTicket = output.tickets().get(0);
        GetTicketsInEmptyStatusUseCase.Output.Item secondTicket = output.tickets().get(1);

        assertEquals("A1", firstTicket.seatNo());
        assertEquals(100, firstTicket.price());
        assertEquals("A2", secondTicket.seatNo());
        assertEquals(120, secondTicket.price());
    }

    @Test
    void test_예약된티켓이있을때() {
        // Given
        Long concertScheduleId = concertSchedule.getId();
        reserveRepository.save(new Reserve(concertScheduleId, 2L, 1L));

        GetTicketsInEmptyStatusUseCase.Input input = new GetTicketsInEmptyStatusUseCase.Input(concertScheduleId);

        // When
        GetTicketsInEmptyStatusUseCase.Output output = getTicketsInEmptyStatusUseCase.execute(input);

        // Then
        assertEquals(1, output.tickets().size());
    }

    @Test
    void test_결제된티켓이있을때() {
        // Given
        Long concertScheduleId = concertSchedule.getId();
        paidTicketRepository.save(concertScheduleId, 2L);

        GetTicketsInEmptyStatusUseCase.Input input = new GetTicketsInEmptyStatusUseCase.Input(concertScheduleId);

        // When
        GetTicketsInEmptyStatusUseCase.Output output = getTicketsInEmptyStatusUseCase.execute(input);

        // Then
        assertEquals(1, output.tickets().size());
    }

    @Test
    void test_예약과결제로_모든좌석이_없을때() {
        // Given
        Long concertScheduleId = concertSchedule.getId();
        reserveRepository.save(new Reserve(concertScheduleId, 1L, 1L));
        paidTicketRepository.save(concertScheduleId, 2L);

        GetTicketsInEmptyStatusUseCase.Input input = new GetTicketsInEmptyStatusUseCase.Input(concertScheduleId);

        // When
        GetTicketsInEmptyStatusUseCase.Output output = getTicketsInEmptyStatusUseCase.execute(input);

        // Then
        assertEquals(0, output.tickets().size());
    }

    @Test
    void test_두번째_호출은_캐시가_사용된다() {
        // Given
        Long concertScheduleId = concertSchedule.getId();

        GetTicketsInEmptyStatusUseCase.Input input = new GetTicketsInEmptyStatusUseCase.Input(concertScheduleId);

        // When
        GetTicketsInEmptyStatusUseCase.Output output = getTicketsInEmptyStatusUseCase.execute(input);
        GetTicketsInEmptyStatusUseCase.Output output2 = getTicketsInEmptyStatusUseCase.execute(input);

        // Then
        Cache cacheTickets = cacheManager.getCache("tickets");
        Cache cacheTicketsInEmpty = cacheManager.getCache("ticketsInEmpty");
        assertNotNull(cacheTickets.get(concertScheduleId));
        assertNotNull(cacheTicketsInEmpty.get(concertScheduleId));
        assertEquals(2, output.tickets().size());
        assertEquals(2, output2.tickets().size());
    }
}
