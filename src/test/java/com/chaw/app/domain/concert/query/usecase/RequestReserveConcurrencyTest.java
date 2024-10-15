package com.chaw.app.domain.concert.query.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.query.usecase.RequestReserve;
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
    void testConcurrencyRequestReserve() throws InterruptedException, ExecutionException {
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

        // 결과 확인
        int successCount = 0;
        int failureCount = 0;

        for (Future<RequestReserve.Output> future : futures) {
            try {
                future.get();
                successCount++;
            } catch (ExecutionException e) {
                failureCount++;
            }
        }

        assertEquals(1, successCount);  // 성공한 요청은 1개여야 함
        assertEquals(4, failureCount);  // 실패한 요청은 4개여야 함

        // 최종적으로 티켓 상태가 TEMP_RESERVATION으로 변경되었는지 확인
        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());

        executorService.shutdown();
    }
}
