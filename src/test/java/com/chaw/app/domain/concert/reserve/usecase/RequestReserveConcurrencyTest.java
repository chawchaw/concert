package com.chaw.app.domain.concert.reserve.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReserved;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.reserve.usecase.RequestReserve;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
public class RequestReserveConcurrencyTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RequestReserve requestReserve;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setStatus(TicketStatus.EMPTY);
        ticket = ticketRepository.save(ticket);
    }

    @AfterEach
    void tearDown() {
        ticketRepository.deleteById(ticket.getId());
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
                RequestReserve.Input input = new RequestReserve.Input(userId, ticket.getId());
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

        // 발생한 예외들이 TicketAlreadyReserved인지 확인
        for (Throwable exception : exceptions) {
            assertTrue(exception instanceof TicketAlreadyReserved);
        }

        executorService.shutdown();
    }
}
