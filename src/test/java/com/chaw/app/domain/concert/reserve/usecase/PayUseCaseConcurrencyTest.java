package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayUseCase;
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
public class PayUseCaseConcurrencyTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReserveRepository reserveRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaidTicketRepository paidTicketRepository;;

    @Autowired
    private PayUseCase payUseCase;

    int THREAD_COUNT = 10;
    private Long userId = 1L;
    private Integer balance = 1000;
    private Integer price = 100;

    private Point point;
    private Concert concert;
    private ConcertSchedule concertSchedule;
    private Ticket ticket;
    private Reserve reserve;

    @BeforeEach
    void setUp() {
        point = Point.builder()
                .userId(userId)
                .balance(balance)
                .build();
        pointRepository.save(point);

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
                .price(price)
                .build();
        ticketRepository.save(ticket);

        reserve = new Reserve(ticket.getConcertScheduleId(), ticket.getId(), userId);
        reserveRepository.save(reserve);
    }

    @FunctionalInterface
    public interface PayTicketRunnable<T> {
        void run(Long userId, Long ticketId);
    }

    void testConcurrency(TestReporter testReporter, PayTicketRunnable runnable) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    runnable.run(userId, ticket.getId());
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

        Point pointNew = pointRepository.findByUserId(userId);
        assertEquals(1000 - 100, pointNew.getBalance());

        Integer countPayment = paymentRepository.countByTicketId(ticket.getId());
        assertEquals(1, countPayment);

        long countPointHistory = pointHistoryRepository.countAll();
        assertEquals(1, countPointHistory);

        int countPaidTicket = paidTicketRepository.countByConcertScheduleId(ticket.getConcertScheduleId());
        assertEquals(1, countPaidTicket);

        System.out.println("사용자수: " + THREAD_COUNT);
        System.out.println("소요시간: " + elapsedTime + "ms");
        testReporter.publishEntry("사용자수", NumberFormat.getInstance().format(THREAD_COUNT));
        testReporter.publishEntry("소요시간", elapsedTime + "ms");

        executorService.shutdown();
    }

    @Test
    void redissonRLock(TestReporter testReporter) throws InterruptedException {
        testConcurrency(testReporter, (userId, ticketId) -> {
            PayUseCase.Input input = new PayUseCase.Input(userId, ticketId);
            payUseCase.execute(input);
        });
    }

}
