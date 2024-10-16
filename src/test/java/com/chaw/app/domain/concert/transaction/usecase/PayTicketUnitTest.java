package com.chaw.app.domain.concert.transaction.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.exception.PointNotFound;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFound;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.exception.IdempotencyNotFound;
import com.chaw.concert.app.domain.concert.transaction.exception.TicketNotInStatusReserve;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import com.chaw.concert.app.domain.concert.transaction.usecase.PayTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PayTicketTest {

    @Mock
    private TicketTransactionRepository ticketTransactionRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private PayTicket payTicket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecute_IdempotencyNotFound() {
        // Given
        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(anyString(), eq(false)))
                .thenReturn(null);

        // When
        PayTicket.Input input = new PayTicket.Input("idempotencyKey", 1L);

        // Then
        assertThrows(IdempotencyNotFound.class, () -> payTicket.execute(input));
    }

    @Test
    void testExecute_TicketNotFound() {
        // Given
        TicketTransaction transaction = new TicketTransaction();
        transaction.setTicketId(1L);
        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(anyString(), eq(false)))
                .thenReturn(transaction);
        when(ticketRepository.findById(anyLong())).thenReturn(null);

        // When
        PayTicket.Input input = new PayTicket.Input("idempotencyKey", 1L);

        // Then
        assertThrows(TicketNotFound.class, () -> payTicket.execute(input));
    }

    @Test
    void testExecute_TicketNotInStatusReserve() {
        // Given
        TicketTransaction transaction = new TicketTransaction();
        transaction.setTicketId(1L);
        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(anyString(), eq(false)))
                .thenReturn(transaction);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID); // RESERVE가 아닌 상태
        when(ticketRepository.findById(anyLong())).thenReturn(ticket);

        // When
        PayTicket.Input input = new PayTicket.Input("idempotencyKey", 1L);

        // Then
        assertThrows(TicketNotInStatusReserve.class, () -> payTicket.execute(input));
    }

    @Test
    void testExecute_PointNotFound() {
        // Given
        TicketTransaction transaction = new TicketTransaction();
        transaction.setTicketId(1L);
        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(anyString(), eq(false)))
                .thenReturn(transaction);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        when(ticketRepository.findById(anyLong())).thenReturn(ticket);

        when(pointRepository.findByUserIdWithLock(anyLong())).thenReturn(null);

        // When
        PayTicket.Input input = new PayTicket.Input("idempotencyKey", 1L);

        // Then
        assertThrows(PointNotFound.class, () -> payTicket.execute(input));
    }

    @Test
    void testExecute_InsufficientBalance() {
        // Given
        TicketTransaction transaction = new TicketTransaction();
        transaction.setTicketId(1L);
        transaction.setAmount(100);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(anyString(), eq(false)))
                .thenReturn(transaction);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        when(ticketRepository.findById(anyLong())).thenReturn(ticket);

        Point point = new Point();
        point.setBalance(50); // 잔액 부족
        when(pointRepository.findByUserIdWithLock(any())).thenReturn(point);

        // When
        PayTicket.Input input = new PayTicket.Input("idempotencyKey", 1L);
        PayTicket.Output output = payTicket.execute(input);

        // Then
        assertEquals(TransactionStatus.FAILED.name(), output.status());
        assertEquals("잔액이 부족합니다.", output.message());
        verify(ticketTransactionRepository, times(1)).save(any(TicketTransaction.class));
    }

    @Test
    void testExecute_SuccessfulTransaction() {
        // Given: 사용자의 포인트 잔액이 충분하고 트랜잭션이 성공한 경우
        TicketTransaction transaction = new TicketTransaction();
        transaction.setTicketId(1L);
        transaction.setAmount(100);
        transaction.setTransactionStatus(TransactionStatus.PENDING);
        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock(anyString(), eq(false)))
                .thenReturn(transaction);

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        when(ticketRepository.findById(anyLong())).thenReturn(ticket);

        Point point = new Point();
        point.setBalance(200); // 잔액 충분
        when(pointRepository.findByUserIdWithLock(any())).thenReturn(point);

        // When: 요청 실행
        PayTicket.Input input = new PayTicket.Input("idempotencyKey", 1L);
        PayTicket.Output output = payTicket.execute(input);

        // Then: 트랜잭션이 성공해야 함
        assertEquals("COMPLETED", output.status());
        assertTrue(output.message().contains("결제가 완료되었습니다."));
        verify(ticketTransactionRepository, times(1)).save(any(TicketTransaction.class));
        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
        verify(pointRepository, times(1)).save(any(Point.class));
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }
}
