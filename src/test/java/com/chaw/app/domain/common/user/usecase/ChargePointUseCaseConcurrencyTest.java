package com.chaw.app.domain.common.user.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.ChargePointRedissonLockUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.text.NumberFormat;
import java.util.List;
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
public class ChargePointUseCaseConcurrencyTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private ChargePointRedissonLockUseCase chargePointRedissonLockUseCase;

    final int THREAD_COUNT = 10;
    final Long userId = 1L;
    final Integer balance = 100;
    final Integer chargeAmount = 50;
    Point point;

    @BeforeEach
    void setUp() {
        point = Point.builder()
                .userId(userId)
                .balance(balance)
                .build();
        pointRepository.save(point);
    }

    @FunctionalInterface
    public interface ChargePointRunnable<T> {
        void run(Long userId, Integer point);
    }

    void testConcurrency(TestReporter testReporter, ChargePointRunnable runnable) throws InterruptedException {
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

                    runnable.run(userId, chargeAmount);
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

        assertEquals(THREAD_COUNT, successCount.get());
        assertEquals(0, failCount.get());

        Point updatedPoint = pointRepository.findByUserId(userId);
        List<PointHistory> pointHistories = pointHistoryRepository.findByPointId(updatedPoint.getId());

        assertEquals(balance + (chargeAmount * THREAD_COUNT), updatedPoint.getBalance());
        assertEquals(THREAD_COUNT, pointHistories.size());

        System.out.println("사용자수: " + THREAD_COUNT);
        System.out.println("소요시간: " + elapsedTime + "ms");
        testReporter.publishEntry("사용자수", NumberFormat.getInstance().format(THREAD_COUNT));
        testReporter.publishEntry("소요시간", elapsedTime + "ms");

        executorService.shutdown();
    }

    @Test
    @DisplayName("낙관락과는 대조로 요청이 많아져도 대기시간을 0초로 설정하여 하나의 요청만 성공합니다.")
    void redissonRLock(TestReporter testReporter) throws InterruptedException {
        testConcurrency(testReporter, (userId, point) -> {
            ChargePointRedissonLockUseCase.Input input = new ChargePointRedissonLockUseCase.Input(userId, chargeAmount);
            chargePointRedissonLockUseCase.execute(input);
        });
    }
}
