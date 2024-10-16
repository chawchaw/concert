package com.chaw.app.domain.concert.transaction.scheduler;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import com.chaw.concert.app.domain.concert.transaction.scheduler.TransactionCleanupScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class TransactionCleanupSchedulerIT {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketTransactionRepository ticketTransactionRepository;

    @Autowired
    private TransactionCleanupScheduler transactionCleanupScheduler;

    private Ticket ticket;
    private TicketTransaction expiredTransaction;

    @BeforeEach
    void setUp() {
        // 예약된 티켓 생성
        ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);
        ticket.setReserveUserId(1L);
        ticket.setReserveEndAt(LocalDateTime.now().minusMinutes(10)); // 10분 전에 예약됨
        ticket = ticketRepository.save(ticket);

        // 만료된 트랜잭션 생성
        expiredTransaction = new TicketTransaction();
        expiredTransaction.setTicketId(ticket.getId());
        expiredTransaction.setTransactionStatus(TransactionStatus.PENDING);
        expiredTransaction.setExpiredAt(LocalDateTime.now().minusMinutes(5)); // 5분 전에 만료됨
        expiredTransaction.setIsDeleted(false);
        expiredTransaction = ticketTransactionRepository.save(expiredTransaction);
    }

    @AfterEach
    void tearDown() {
        ticketTransactionRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    @Transactional
    void testCleanupExpiredTransactions() {
        // When: 스케줄러를 실행
        transactionCleanupScheduler.cleanupExpiredTransactions();

        // Then: 트랜잭션 상태가 EXPIRED로 변경되고 삭제되었는지 확인
        TicketTransaction updatedTransaction = ticketTransactionRepository.findById(expiredTransaction.getId());
        assertEquals(TransactionStatus.EXPIRED, updatedTransaction.getTransactionStatus());
        assertEquals(true, updatedTransaction.getIsDeleted());

        // 티켓 상태가 EMPTY로 변경되었는지 확인
        Ticket updatedTicket = ticketRepository.findById(ticket.getId());
        assertEquals(TicketStatus.EMPTY, updatedTicket.getStatus());
        assertEquals(null, updatedTicket.getReserveUserId());
        assertEquals(null, updatedTicket.getReserveEndAt());
    }
}
