package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserveOptimisticLockUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReservePessimistickLockUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserveRedissonRLockUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class RequestReserveUseCaseConcurrencyTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RequestReservePessimistickLockUseCase requestReservePessimistickLockUseCase;

    @Autowired
    private RequestReserveOptimisticLockUseCase requestReserveOptimisticLockUseCase;

    @Autowired
    private RequestReserveRedissonRLockUseCase requestReserveRedissonRLockUseCase;

    private Concert concert1;
    private ConcertSchedule concertSchedule1;
    private Ticket ticket1;
    private Ticket ticket2;
    int THREAD_COUNT = 5000;

    @BeforeEach
    void setUp() {
        concert1 = Concert.builder()
                .name("concert1")
                .build();
        concertRepository.save(concert1);

        concertSchedule1 = ConcertSchedule.builder()
                .concertId(concert1.getId())
                .isSoldOut(false)
                .totalSeat(10)
                .availableSeat(10)
                .dateConcert(LocalDateTime.now().plusDays(1))
                .build();
        concertScheduleRepository.save(concertSchedule1);

        ticket1 = Ticket.builder()
                .concertScheduleId(concertSchedule1.getId())
                .status(TicketStatus.EMPTY)
                .build();
        ticketRepository.save(ticket1);

        ticket2 = Ticket.builder()
                .concertScheduleId(concertSchedule1.getId())
                .status(TicketStatus.EMPTY)
                .build();
        ticketRepository.save(ticket2);
    }

    @FunctionalInterface
    public interface RequestReserveRunnable<T> {
        void run(Long userId, Long ticketId);
    }

    void testCommon(TestReporter testReporter, RequestReserveRunnable runnable) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Long userId = (long) i + 1;
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    runnable.run(userId, ticket1.getId());
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        Long startTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        Long endTime = System.currentTimeMillis();
        Long elapsedTime = endTime - startTime;

        assertEquals(1, successCount.get());
        assertEquals(THREAD_COUNT - 1, failCount.get());

        Ticket updatedTicket = ticketRepository.findByIdOrThrow(ticket1.getId());
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());

        System.out.println("사용자수: " + THREAD_COUNT);
        System.out.println("소요시간: " + elapsedTime + "ms");
        testReporter.publishEntry("사용자수", NumberFormat.getInstance().format(THREAD_COUNT));
        testReporter.publishEntry("소요시간", elapsedTime + "ms");

        executorService.shutdown();
    }

    @Test
    void optimisticLock(TestReporter testReporter) throws InterruptedException {
        testCommon(testReporter, (userId, ticketId) -> {
            RequestReserveOptimisticLockUseCase.Input input = new RequestReserveOptimisticLockUseCase.Input(userId, ticket1.getId());
            requestReserveOptimisticLockUseCase.execute(input);
        });
    }

    @Test
    void pessimisticLock(TestReporter testReporter) throws InterruptedException {
        testCommon(testReporter, (userId, ticketId) -> {
            RequestReservePessimistickLockUseCase.Input input = new RequestReservePessimistickLockUseCase.Input(userId, ticket1.getId());
            requestReservePessimistickLockUseCase.execute(input);
        });
    }

    @Test
    void redissonRLock(TestReporter testReporter) throws InterruptedException {
        testCommon(testReporter, (userId, ticketId) -> {
            RequestReserveRedissonRLockUseCase.Input input = new RequestReserveRedissonRLockUseCase.Input(userId, ticket1.getId());
            requestReserveRedissonRLockUseCase.execute(input);
        });
    }

}
