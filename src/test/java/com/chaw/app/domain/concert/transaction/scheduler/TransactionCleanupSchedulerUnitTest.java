package com.chaw.app.domain.concert.transaction.scheduler;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionCleanupSchedulerUnitTest {

    @Mock
    private TicketTransactionRepository ticketTransactionRepository;

    @InjectMocks
    private TransactionCleanupScheduler transactionCleanupScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCleanupExpiredTransactions() {
        // Given
        TicketTransaction transaction1 = new TicketTransaction();
        transaction1.setTransactionStatus(TransactionStatus.PENDING);
        transaction1.setIsDeleted(false);

        TicketTransaction transaction2 = new TicketTransaction();
        transaction2.setTransactionStatus(TransactionStatus.PENDING);
        transaction2.setIsDeleted(false);

        List<TicketTransaction> expiredTransactions = Arrays.asList(transaction1, transaction2);

        // When
        when(ticketTransactionRepository.findByTransactionStatusNotCompletedAndExpiredAtBeforeAndNotDeleted(any(LocalDateTime.class)))
                .thenReturn(expiredTransactions);

        // Then: 각 트랜잭션이 EXPIRED 상태로 업데이트 되었는지 확인
        transactionCleanupScheduler.cleanupExpiredTransactions();
        verify(ticketTransactionRepository, times(2)).save(any(TicketTransaction.class));

        // 트랜잭션 상태가 EXPIRED로 변경되었는지 검증
        assertEquals(transaction1.getTransactionStatus(), TransactionStatus.EXPIRED);
        assertEquals(transaction2.getTransactionStatus(), TransactionStatus.EXPIRED);

        // 트랜잭션이 소프트 삭제되었는지 검증
        assertTrue(transaction1.getIsDeleted());
        assertTrue(transaction2.getIsDeleted());
    }
}
