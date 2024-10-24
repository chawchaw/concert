package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class RequestReserveConcurrencyTest {

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
                .build();
        ticketRepository.save(ticket);
    }

    @Test
    void testConcurrencyRequestReserve() throws InterruptedException {
        // 스레드 수를 5로 설정
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<RequestReserve.Output>> futures = new ArrayList<>();

        // 5명의 사용자에게 같은 티켓을 동시에 예약하도록 요청
        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) i + 1;
            futures.add(executorService.submit(() -> {
                RequestReserve.Input input = new RequestReserve.Input(userId, concert.getId(), concertSchedule.getId(), ticket.getId());
                return requestReserve.execute(input);
            }));
        }

        // 성공 및 실패 결과 확인
        int successCount = 0;
        int failureCount = 0;
        List<Throwable> exceptions = new ArrayList<>();

        for (Future<RequestReserve.Output> future : futures) {
            try {
                future.get();  // 스레드가 실패하면 ExecutionException이 발생
                successCount++;
            } catch (ExecutionException e) {
                failureCount++;
                // 발생한 예외 수집
                exceptions.add(e.getCause());
            }
        }

        // 하나의 스레드만 성공해야 함
        assertEquals(1, successCount);
        // 나머지 4개의 스레드는 실패해야 함
        assertEquals(4, failureCount);

        // 티켓의 상태가 최종적으로 RESERVE로 변경되었는지 확인
        Ticket updatedTicket = ticketRepository.findById(ticket.getId());
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());

        for (Throwable exception : exceptions) {
            BaseException baseException = (BaseException) exception;
            assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
        }

        executorService.shutdown();
    }
}
