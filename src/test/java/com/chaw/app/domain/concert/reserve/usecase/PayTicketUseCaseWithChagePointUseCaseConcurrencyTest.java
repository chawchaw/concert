package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.ChargePointRedissonLockUseCase;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueueStatus;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.repository.PaymentRepository;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketPessimistickUseCase;
import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicketRedissonRLockUseCase;
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
public class PayTicketUseCaseWithChagePointUseCaseConcurrencyTest {

    @Autowired
    private WaitQueueRepository waitQueueRepository;

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
    private ChargePointRedissonLockUseCase chargePointRedissonLockUseCase;

    @Autowired
    private PayTicketRedissonRLockUseCase payTicketRedissonRLockUseCase;

    int THREAD_COUNT = 3;
    private Long userId = 1L;
    private Integer balance = 1000;
    private Integer price = 100;
    private Integer chargePoint = 50;

    private Point point;
    private Concert concert;
    private ConcertSchedule concertSchedule;
    private WaitQueue waitQueue;
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

        waitQueue = WaitQueue.builder()
                .userId(userId)
                .status(WaitQueueStatus.PASS)
                .build();
        waitQueueRepository.save(waitQueue);

        ticket = Ticket.builder()
                .concertScheduleId(concertSchedule.getId())
                .status(TicketStatus.RESERVE)
                .price(price)
                .reserveUserId(userId)
                .build();
        ticketRepository.save(ticket);

        reserve = Reserve.builder()
                .userId(userId)
                .ticketId(ticket.getId())
                .reserveStatus(ReserveStatus.RESERVE)
                .amount(ticket.getPrice())
                .createdAt(LocalDateTime.now())
                .build();
        reserveRepository.save(reserve);
    }

    @FunctionalInterface
    public interface PayTicketRunnable<T> {
        void run(Long userId, Long ticketId);
    }

    @FunctionalInterface
    public interface ChargePointRunnable<T> {
        void run(Long userId, Integer point);
    }

    void testConcurrency(TestReporter testReporter, PayTicketRunnable payTicketRunnable, ChargePointRunnable chargePointRunnable) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT * 2);

        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT * 2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT * 2);

        AtomicInteger successPayTicketCount = new AtomicInteger(0);
        AtomicInteger successChargePointCount = new AtomicInteger(0);
        AtomicInteger failPayTicketCount = new AtomicInteger(0);
        AtomicInteger failChargePointCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    payTicketRunnable.run(userId, ticket.getId());
                    successPayTicketCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    failPayTicketCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    chargePointRunnable.run(userId, chargePoint);
                    successChargePointCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    failChargePointCount.incrementAndGet();
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

        assertEquals(1, successPayTicketCount.get());
        assertEquals(THREAD_COUNT - 1, failPayTicketCount.get());
        assertEquals(THREAD_COUNT, successChargePointCount.get());
        assertEquals(0, failChargePointCount.get());

        Point pointNew = pointRepository.findByUserId(userId);
        assertEquals(1000 - (100 * 1) + (chargePoint * THREAD_COUNT), pointNew.getBalance());

        Integer countPayment = paymentRepository.countByReserveId(reserve.getId());
        assertEquals(1, countPayment);

        long countPointHistory = pointHistoryRepository.countAll();
        assertEquals(1 + THREAD_COUNT, countPointHistory);

        System.out.println("사용자수: " + THREAD_COUNT);
        System.out.println("소요시간: " + elapsedTime + "ms");
        testReporter.publishEntry("사용자수", NumberFormat.getInstance().format(THREAD_COUNT));
        testReporter.publishEntry("소요시간", elapsedTime + "ms");

        executorService.shutdown();
    }

    @Test
    void redissonRLock(TestReporter testReporter) throws InterruptedException {
        testConcurrency(testReporter, (userId, ticketId) -> {
            PayTicketRedissonRLockUseCase.Input input = new PayTicketRedissonRLockUseCase.Input(userId, ticketId);
            payTicketRedissonRLockUseCase.execute(input);
        }, (userId, point) -> {
            ChargePointRedissonLockUseCase.Input input = new ChargePointRedissonLockUseCase.Input(userId, point);
            chargePointRedissonLockUseCase.execute(input);
        });
    }

}
