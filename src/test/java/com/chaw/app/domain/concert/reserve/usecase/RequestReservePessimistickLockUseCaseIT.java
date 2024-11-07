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
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReservePessimistickLockUseCase;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class RequestReservePessimistickLockUseCaseIT {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private RequestReservePessimistickLockUseCase requestReservePessimistickLockUseCase;

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

        RequestReservePessimistickLockUseCase.Input input = new RequestReservePessimistickLockUseCase.Input(userId, ticketId);

        // When
        RequestReservePessimistickLockUseCase.Output output = requestReservePessimistickLockUseCase.execute(input);

        // Then
        assertNotNull(output);
        assertEquals(true, output.success());

        Ticket updatedTicket = ticketRepository.findByIdOrThrow(ticketId);
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());

        Reserve reserve = reserveRepository.findByTicketIdOrThrow(ticketId);
        assertEquals(ReserveStatus.RESERVE, reserve.getReserveStatus());
        assertEquals(ticketId, reserve.getTicketId());
        assertEquals(userId, reserve.getUserId());
    }

    @Test
    void testExecute_Fail_Already_Reserved() {
        // given
        Long userId = 1L;
        Long ticketId = ticket.getId();
        ticket.reserve();
        ticketRepository.save(ticket);

        RequestReservePessimistickLockUseCase.Input input = new RequestReservePessimistickLockUseCase.Input(userId, ticketId);

        // when
        BaseException exception = assertThrows(BaseException.class, () -> requestReservePessimistickLockUseCase.execute(input));

        // then
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }
}
