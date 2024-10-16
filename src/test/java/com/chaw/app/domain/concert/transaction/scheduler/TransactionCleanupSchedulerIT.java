package com.chaw.app.domain.concert.transaction.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import com.chaw.concert.app.domain.concert.transaction.scheduler.TransactionCleanupScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class TransactionCleanupSchedulerIT {

    @Autowired
    private TicketTransactionRepository ticketTransactionRepository;

    @Autowired
    private TransactionCleanupScheduler transactionCleanupScheduler;

    @BeforeEach
    void setUp() {
        // When 트랜잭션 2개를 미리 생성해서 저장
        TicketTransaction transaction1 = new TicketTransaction();
        transaction1.setTransactionStatus(TransactionStatus.PENDING);
        transaction1.setExpiredAt(LocalDateTime.now().minusMinutes(5)); // 이미 만료된 트랜잭션
        transaction1.setIsDeleted(false);
        ticketTransactionRepository.save(transaction1);

        TicketTransaction transaction2 = new TicketTransaction();
        transaction2.setTransactionStatus(TransactionStatus.PENDING);
        transaction2.setExpiredAt(LocalDateTime.now().minusMinutes(5)); // 이미 만료된 트랜잭션
        transaction2.setIsDeleted(false);
        ticketTransactionRepository.save(transaction2);
    }

    @Test
    void testCleanupFailedTransactions() {
        // Then
        transactionCleanupScheduler.cleanupExpiredTransactions();

        // Verify
        List<TicketTransaction> expiredTransactions = ticketTransactionRepository.findByTransactionStatus(TransactionStatus.EXPIRED);

        assertEquals(2, expiredTransactions.size());
        for (TicketTransaction transaction : expiredTransactions) {
            assertEquals(TransactionStatus.EXPIRED, transaction.getTransactionStatus());
            assertTrue(transaction.getIsDeleted());
        }
    }
}
