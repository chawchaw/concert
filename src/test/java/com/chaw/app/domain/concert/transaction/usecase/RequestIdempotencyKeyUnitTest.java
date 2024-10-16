package com.chaw.app.domain.concert.transaction.usecase;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestIdempotencyKeyUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTransactionRepository ticketTransactionRepository;

    @InjectMocks
    private RequestIdempotencyKey requestIdempotencyKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // EXPIRED_MINUTES 값을 설정
        ReflectionTestUtils.setField(requestIdempotencyKey, "EXPIRED_MINUTES", 10);
    }

    @Test
    void testExecute_TicketNotFound() {
        // Given
        when(ticketRepository.findById(anyLong())).thenReturn(null);

        // When
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(1L, 1L);

        // Then
        assertThrows(TicketNotFound.class, () -> requestIdempotencyKey.execute(input));
    }

    @Test
    void testExecute_TicketNotInReserveStatus() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);
        when(ticketRepository.findById(anyLong())).thenReturn(ticket);

        // When
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(1L, 1L);

        // Then
        assertThrows(TicketNotInStatusReserve.class, () -> requestIdempotencyKey.execute(input));
    }

    @Test
    void testExecute_NewTransactionCreated() {
        // Given: 티켓이 RESERVE 상태이고 트랜잭션이 존재하지 않음
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        ticket.setPrice(100);

        when(ticketRepository.findById(anyLong())).thenReturn(ticket);
        when(ticketTransactionRepository.findByUserIdAndTicketIdAndIsDeleted(anyLong(), anyLong(), eq(false)))
                .thenReturn(null);

        // When: 새로운 트랜잭션이 생성되어야 함
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(1L, 1L);
        RequestIdempotencyKey.Output output = requestIdempotencyKey.execute(input);

        // Then: 새로운 트랜잭션이 저장되어야 함
        verify(ticketTransactionRepository, times(1)).save(any(TicketTransaction.class));
        assertNotNull(output.idempotencyKey());
        assertEquals(TransactionStatus.PENDING.name(), output.status());
    }

    @Test
    void testExecute_ExistingTransactionReturned() {
        // Given: 이미 트랜잭션이 존재하는 상황
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        ticket.setPrice(100);

        TicketTransaction existingTransaction = new TicketTransaction();
        existingTransaction.setIdempotencyKey("existing-key");
        existingTransaction.setTransactionStatus(TransactionStatus.PENDING);

        when(ticketRepository.findById(anyLong())).thenReturn(ticket);
        when(ticketTransactionRepository.findByUserIdAndTicketIdAndIsDeleted(anyLong(), anyLong(), eq(false)))
                .thenReturn(existingTransaction);

        // When: 이미 존재하는 트랜잭션을 반환해야 함
        RequestIdempotencyKey.Input input = new RequestIdempotencyKey.Input(1L, 1L);
        RequestIdempotencyKey.Output output = requestIdempotencyKey.execute(input);

        // Then: 기존 트랜잭션의 IdempotencyKey와 상태가 반환되어야 함
        assertEquals("existing-key", output.idempotencyKey());
        assertEquals(TransactionStatus.PENDING.name(), output.status());

        // 새로운 트랜잭션은 생성되지 않음
        verify(ticketTransactionRepository, never()).save(any(TicketTransaction.class));
    }
}
