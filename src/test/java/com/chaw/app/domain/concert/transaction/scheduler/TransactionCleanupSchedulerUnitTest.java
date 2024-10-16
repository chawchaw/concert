package com.chaw.app.domain.concert.transaction.scheduler;

import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import com.chaw.concert.app.domain.concert.transaction.scheduler.TransactionCleanupScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionCleanupSchedulerUnitTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTransactionRepository ticketTransactionRepository;

    @InjectMocks
    private TransactionCleanupScheduler transactionCleanupScheduler;

    private TicketTransaction expiredTransaction;
    private Ticket reservedTicket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 만료된 트랜잭션 설정
        expiredTransaction = new TicketTransaction();
        expiredTransaction.setTicketId(1L);
        expiredTransaction.setTransactionStatus(TransactionStatus.PENDING);
        expiredTransaction.setExpiredAt(LocalDateTime.now().minusMinutes(10));  // 이미 만료된 시간
        expiredTransaction.setIsDeleted(false);

        // 예약된 티켓 설정
        reservedTicket = new Ticket();
        reservedTicket.setId(1L);
        reservedTicket.setStatus(TicketStatus.RESERVE);
        reservedTicket.setReserveUserId(1L);
        reservedTicket.setReserveEndAt(LocalDateTime.now().minusMinutes(10));
    }

    @Test
    void testCleanupExpiredTransactions() {
        // Given: 만료된 트랜잭션 리스트를 반환
        List<TicketTransaction> expiredTransactions = Collections.singletonList(expiredTransaction);
        when(ticketTransactionRepository.findByTransactionStatusNotCompletedAndExpiredAtBeforeAndNotDeleted(any(LocalDateTime.class)))
                .thenReturn(expiredTransactions);

        // 티켓이 예약된 상태로 조회
        when(ticketRepository.findById(expiredTransaction.getTicketId()))
                .thenReturn(reservedTicket);

        // When: 스케줄러 메서드를 호출
        transactionCleanupScheduler.cleanupExpiredTransactions();

        // Then: 트랜잭션이 만료 상태로 업데이트되고 삭제 처리되었는지 확인
        verify(ticketTransactionRepository, times(1)).save(expiredTransaction);
        assertEquals(expiredTransaction.getTransactionStatus(), TransactionStatus.EXPIRED);
        assertTrue(expiredTransaction.getIsDeleted());

        // 티켓 상태가 EMPTY로 변경되었는지 확인
        verify(ticketRepository, times(1)).save(reservedTicket);
        assertEquals(reservedTicket.getStatus(), TicketStatus.EMPTY);
        assertNull(reservedTicket.getReserveUserId());
        assertNull(reservedTicket.getReserveEndAt());
    }
}
