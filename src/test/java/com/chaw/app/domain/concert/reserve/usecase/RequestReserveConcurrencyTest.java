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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        // 스레드 수를 5로 설정
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<RequestReserve.Output>> futures = new ArrayList<>();

        // 5명의 사용자에게 같은 티켓을 동시에 예약하도록 요청
        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) i + 1;
            futures.add(executorService.submit(() -> {
                RequestReserve.Input input = new RequestReserve.Input(userId, concert1.getId(), concertSchedule1.getId(), ticket1.getId());
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
        Ticket updatedTicket = ticketRepository.findById(ticket1.getId());
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());

        for (Throwable exception : exceptions) {
            BaseException baseException = (BaseException) exception;
            assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
        }

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
                    RequestReserve.Input input = new RequestReserve.Input(userId, 1L, 1L, 1L);
                    requestReserve.execute(input);
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
                    RequestReserve.Input input = new RequestReserve.Input(userId, 1L, 1L, 2L);
                    requestReserve.execute(input);
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
        Ticket reservedTicket1 = ticketRepository.findById(1L);
        Ticket reservedTicket2 = ticketRepository.findById(2L);

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
