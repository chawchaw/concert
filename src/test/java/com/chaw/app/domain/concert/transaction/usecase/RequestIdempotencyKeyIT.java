package com.chaw.app.domain.concert.transaction.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.exception.TicketNotInStatusReserve;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import com.chaw.concert.app.domain.concert.transaction.usecase.RequestIdempotencyKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class RequestIdempotencyKeyIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketTransactionRepository ticketTransactionRepository;

    @Autowired
    private RequestIdempotencyKey requestIdempotencyKey;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        ticket.setPrice(100);
        ticket = ticketRepository.save(ticket);
    }

    @Test
    void testExecute_NewTransactionCreated() {
        // Given: 티켓이 존재하고, 해당 사용자에 대한 트랜잭션이 존재하지 않음
        Long userId = 1L;

        // When: 트랜잭션 생성 요청
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(ticket.getId(), userId);
        RequestIdempotencyKey.Output output = requestIdempotencyKey.execute(input);

        // Then: 새로운 트랜잭션이 생성되었는지 확인
        TicketTransaction createdTransaction = ticketTransactionRepository.findByUserIdAndTicketIdAndIsDeleted(userId, ticket.getId(), false);
        assertNotNull(createdTransaction); // 트랜잭션이 생성되었는지 확인
        assertEquals(TransactionStatus.PENDING, createdTransaction.getTransactionStatus()); // 상태가 PENDING인지 확인
        assertEquals(output.idempotencyKey(), createdTransaction.getIdempotencyKey()); // 응답의 idempotencyKey와 저장된 트랜잭션의 idempotencyKey 비교
    }

    @Test
    void testExecute_ExistingTransactionReturned() {
        // Given: 이미 존재하는 트랜잭션 설정
        Long userId = 1L;
        TicketTransaction existingTransaction = TicketTransaction.builder()
                .ticketId(ticket.getId())
                .userId(userId)
                .idempotencyKey("existing-key")
                .transactionStatus(TransactionStatus.PENDING)
                .amount(ticket.getPrice())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .isDeleted(false)
                .build();
        ticketTransactionRepository.save(existingTransaction);

        // When: 동일한 요청이 들어올 때
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(ticket.getId(), userId);
        RequestIdempotencyKey.Output output = requestIdempotencyKey.execute(input);

        // Then: 기존 트랜잭션이 반환되는지 확인
        assertEquals("existing-key", output.idempotencyKey()); // 기존 트랜잭션의 키가 반환되어야 함
        assertEquals(TransactionStatus.PENDING.name(), output.status()); // 트랜잭션 상태도 동일해야 함
    }

    @Test
    void testExecute_TicketNotFound() {
        // Given: 존재하지 않는 티켓 ID
        Long invalidTicketId = 999L;
        Long userId = 1L;

        // When: 존재하지 않는 티켓으로 요청 시
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(invalidTicketId, userId);

        // Then: TicketNotFound 예외가 발생해야 함
        assertThrows(TicketNotFound.class, () -> requestIdempotencyKey.execute(input));
    }

    @Test
    void testExecute_TicketNotInReserveStatus() {
        // Given: 티켓이 RESERVE 상태가 아닌 경우
        ticket.setStatus(TicketStatus.PAID);
        ticketRepository.save(ticket); // 상태 변경 후 저장

        // When: RESERVE 상태가 아닌 티켓으로 요청 시
        Long userId = 1L;
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(ticket.getId(), userId);

        // Then: TicketNotInStatusReserve 예외가 발생해야 함
        assertThrows(TicketNotInStatusReserve.class, () -> requestIdempotencyKey.execute(input));
    }
}
