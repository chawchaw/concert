package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.ConcertRepository;
import com.chaw.concert.app.domain.concert.query.repository.ConcertScheduleRepository;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReservePessimistickLockUseCase;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class RequestReservePessimistickLockUseCaseConcurrencyTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RequestReservePessimistickLockUseCase requestReservePessimistickLockUseCase;

    private Concert concert1;
    private ConcertSchedule concertSchedule1;
    private Ticket ticket1;
    private Ticket ticket2;

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

    @Test
    void testConcurrencyRequestReserve() throws InterruptedException {
        // given
        int threadCount = 7000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) i + 1;
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    RequestReservePessimistickLockUseCase.Input input = new RequestReservePessimistickLockUseCase.Input(userId, ticket1.getId());
                    requestReservePessimistickLockUseCase.execute(input);
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
        assertEquals(threadCount - 1, failCount.get());

        Ticket updatedTicket = ticketRepository.findByIdOrThrow(ticket1.getId());
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());

        System.out.println("소요시간: " + elapsedTime + "ms");

        executorService.shutdown();
    }

    @Test
    void 티켓2장에_대해_티켓1은_7명_티켓2는_3명이_동시_요청() throws InterruptedException {
        // given
        int ticket1Users = 7; // ticket1을 예약하려는 사용자 수
        int ticket2Users = 3; // ticket2를 예약하려는 사용자 수
        int totalUsers = ticket1Users + ticket2Users; // 총 사용자 수

        CountDownLatch readyLatch = new CountDownLatch(totalUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalUsers);

        AtomicInteger successTicket1 = new AtomicInteger(0);
        AtomicInteger successTicket2 = new AtomicInteger(0);
        AtomicInteger failTicket1 = new AtomicInteger(0);
        AtomicInteger failTicket2 = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(totalUsers);

        // when
        // ticket1을 예약하려는 7명의 사용자
        for (int i = 0; i < ticket1Users; i++) {
            final long userId = i + 1;
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    // ticket1 예약
                    RequestReservePessimistickLockUseCase.Input input = new RequestReservePessimistickLockUseCase.Input(userId, 1L);
                    requestReservePessimistickLockUseCase.execute(input);
                    successTicket1.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    failTicket1.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // ticket2을 예약하려는 3명의 사용자
        for (int i = 0; i < ticket2Users; i++) {
            final long userId = i + 1 + ticket1Users; // 사용자 ID는 ticket1 예약자 이후로 시작
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    // ticket2 예약
                    RequestReservePessimistickLockUseCase.Input input = new RequestReservePessimistickLockUseCase.Input(userId, 2L);
                    requestReservePessimistickLockUseCase.execute(input);
                    successTicket2.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    failTicket2.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        // then
        // 티켓1이 예약된 수와 티켓2가 예약된 수가 각각 정확한지 확인
        Ticket reservedTicket1 = ticketRepository.findByIdOrThrow(1L);
        Ticket reservedTicket2 = ticketRepository.findByIdOrThrow(2L);

        assertEquals(TicketStatus.RESERVE, reservedTicket1.getStatus()); // 티켓1 예약 확인
        assertEquals(TicketStatus.RESERVE, reservedTicket2.getStatus()); // 티켓2 예약 확인

        assertEquals(1, successTicket1.get()); // 티켓1 한명만 성공
        assertEquals(1, successTicket2.get()); // 티켓2 한명만 성공
        assertEquals(ticket1Users - 1, failTicket1.get()); // 티켓1 한명을 제외한 모두 실패
        assertEquals(ticket2Users - 1, failTicket2.get()); // 티켓2 한명을 제외한 모두 실패

        executorService.shutdown(); // 스레드풀 종료
        executorService.awaitTermination(1, TimeUnit.MINUTES); // 스레드풀 종료를 기다림
    }
}
