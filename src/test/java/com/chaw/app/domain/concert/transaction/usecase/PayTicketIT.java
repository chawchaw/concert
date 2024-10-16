package com.chaw.app.domain.concert.transaction.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
import com.chaw.concert.app.domain.concert.transaction.entity.TicketTransaction;
import com.chaw.concert.app.domain.concert.transaction.entity.TransactionStatus;
import com.chaw.concert.app.domain.concert.transaction.repository.TicketTransactionRepository;
import com.chaw.concert.app.domain.concert.transaction.usecase.PayTicket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
public class PayTicketIT {

    @Autowired
    private TicketTransactionRepository ticketTransactionRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PayTicket payTicket;

    private Long userId = 1L;
    private Ticket ticket;
    private Point point;
    private TicketTransaction ticketTransaction;

    @BeforeEach
    void setUp() {
        // 티켓 설정
        ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE); // 예약 가능한 상태
        ticket.setPrice(100);
        ticket = ticketRepository.save(ticket);

        // 포인트 설정
        point = new Point();
        point.setUserId(userId);
        point.setBalance(200); // 충분한 잔액
        pointRepository.save(point);

        // 트랜잭션 설정
        ticketTransaction = new TicketTransaction();
        ticketTransaction.setTicketId(ticket.getId());
        ticketTransaction.setUserId(point.getUserId()); // 포인트의 사용자
        ticketTransaction.setAmount(100);
        ticketTransaction.setTransactionStatus(TransactionStatus.PENDING);
        ticketTransaction.setIdempotencyKey("test-key");
        ticketTransaction.setCreatedAt(LocalDateTime.now());
        ticketTransaction.setUpdatedAt(LocalDateTime.now());
        ticketTransaction.setIsDeleted(false);
        ticketTransactionRepository.save(ticketTransaction);
    }

    @AfterEach
    void tearDown() {
        pointHistoryRepository.deleteAll();
        ticketTransactionRepository.deleteAll();
        pointRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    void testSuccessfulPayment() {
        // Given: 충분한 잔액과 PENDING 상태의 트랜잭션이 있을 때
        PayTicket.Input input = new PayTicket.Input("test-key", userId);

        // When: 결제 요청을 실행
        PayTicket.Output output = payTicket.execute(input);

        // Then: 결제가 성공적으로 완료되었는지 확인
        assertEquals("COMPLETED", output.status());
        assertTrue(output.message().contains("결제가 완료되었습니다."));

        // 트랜잭션이 COMPLETED 상태로 변경되었는지 확인
        TicketTransaction updatedTransaction = ticketTransactionRepository.findById(ticketTransaction.getId());
        assertEquals(TransactionStatus.COMPLETED, updatedTransaction.getTransactionStatus());

        // 포인트 잔액이 적절히 차감되었는지 확인
        Point updatedPoint = pointRepository.findById(point.getId());
        assertEquals(100, updatedPoint.getBalance());

        // 포인트 히스토리가 저장되었는지 확인
        PointHistory pointHistory = pointHistoryRepository.findAll().get(0);
        assertEquals(PointHistoryType.PAY, pointHistory.getType());
        assertEquals(100, pointHistory.getAmount());

        // 티켓 상태가 PAID로 변경되었는지 확인
        Ticket updatedTicket = ticketRepository.findById(ticket.getId());
        assertEquals(TicketStatus.PAID, updatedTicket.getStatus());
    }

    @Test
    void testPaymentWithInsufficientBalance() {
        // Given: 사용자의 포인트 잔액이 부족한 경우
        point.setBalance(50); // 잔액을 부족하게 설정
        pointRepository.save(point);

        PayTicket.Input input = new PayTicket.Input("test-key", point.getId());

        // When: 결제 요청을 실행
        PayTicket.Output output = payTicket.execute(input);

        // Then: 잔액 부족으로 결제가 실패했는지 확인
        assertEquals("FAILED", output.status());
        assertTrue(output.message().contains("잔액이 부족합니다."));

        // 트랜잭션이 FAILED 상태로 변경되었는지 확인
        TicketTransaction updatedTransaction = ticketTransactionRepository.findById(ticketTransaction.getId());
        assertEquals(TransactionStatus.FAILED, updatedTransaction.getTransactionStatus());

        // 포인트 잔액이 그대로 유지되었는지 확인
        Point updatedPoint = pointRepository.findById(point.getId());
        assertEquals(50, updatedPoint.getBalance());

        // 포인트 히스토리가 생성되지 않았는지 확인
        assertTrue(pointHistoryRepository.findAll().isEmpty());

        // 티켓 상태는 그대로 RESERVE로 남아 있어야 함
        Ticket updatedTicket = ticketRepository.findById(ticket.getId());
        assertEquals(TicketStatus.RESERVE, updatedTicket.getStatus());
    }

    @Test
    void testAlreadyCompletedTransaction() {
        // Given: 이미 완료된 트랜잭션이 있을 때
        ticketTransaction.setTransactionStatus(TransactionStatus.COMPLETED);
        ticketTransactionRepository.save(ticketTransaction);

        PayTicket.Input input = new PayTicket.Input("test-key", point.getId());

        // When: 이미 완료된 트랜잭션으로 결제 요청을 실행
        PayTicket.Output output = payTicket.execute(input);

        // Then: 결제가 이미 완료되었다는 메시지가 반환되어야 함
        assertEquals("COMPLETED", output.status());
        assertTrue(output.message().contains("이미 결제가 완료되었습니다."));
    }
}
