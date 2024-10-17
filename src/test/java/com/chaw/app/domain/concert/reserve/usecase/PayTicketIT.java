//package com.chaw.app.domain.concert.reserve.usecase;
//
//import com.chaw.concert.ConcertApplication;
//import com.chaw.concert.app.domain.common.user.entity.Point;
//import com.chaw.concert.app.domain.common.user.entity.PointHistory;
//import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
//import com.chaw.concert.app.domain.common.user.repository.PointRepository;
//import com.chaw.concert.app.domain.concert.query.entity.Ticket;
//import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
//import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
//import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
//import com.chaw.concert.app.domain.concert.reserve.entity.TransactionStatus;
//import com.chaw.concert.app.domain.concert.reserve.repository.TicketTransactionRepository;
//import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest(classes = ConcertApplication.class)
//@ExtendWith(SpringExtension.class)
//public class PayTicketIT {
//
//    @Autowired
//    private TicketTransactionRepository ticketTransactionRepository;
//
//    @Autowired
//    private PointRepository pointRepository;
//
//    @Autowired
//    private PointHistoryRepository pointHistoryRepository;
//
//    @Autowired
//    private TicketRepository ticketRepository;
//
//    @Autowired
//    private PayTicket payTicket;
//
//    private Long userId = 1L;
//    private Ticket ticket;
//    private Point point;
//    private Reserve reserve;
//
//    @BeforeEach
//    void setUp() {
//        // 티켓 설정
//        ticket = new Ticket();
//        ticket.setStatus(TicketStatus.RESERVE); // 예약 가능한 상태
//        ticket.setPrice(100);
//        ticket = ticketRepository.save(ticket);
//
//        // 포인트 설정
//        point = new Point();
//        point.setUserId(userId);
//        point.setBalance(200); // 충분한 잔액
//        pointRepository.save(point);
//
//        // 트랜잭션 설정
//        reserve = new Reserve();
//        reserve.setTicketId(ticket.getId());
//        reserve.setUserId(point.getUserId()); // 포인트의 사용자
//        reserve.setAmount(100);
//        reserve.setTransactionStatus(TransactionStatus.PENDING);
//        reserve.setIdempotencyKey("test-key");
//        reserve.setCreatedAt(LocalDateTime.now());
//        reserve.setUpdatedAt(LocalDateTime.now());
//        reserve.setExpiredAt(LocalDateTime.now().plusMinutes(1));
//        reserve.setIsDeleted(false);
//        ticketTransactionRepository.save(reserve);
//    }
//
//    @AfterEach
//    void tearDown() {
//        pointHistoryRepository.deleteAll();
//        ticketTransactionRepository.deleteAll();
//        pointRepository.deleteAll();
//        ticketRepository.deleteAll();
//    }
//
//    @Test
//    void testExecute_SuccessfulPayment() {
//        // Given: 결제 요청 생성
//        PayTicket.Input input = new PayTicket.Input(reserve.getIdempotencyKey(), point.getUserId());
//
//        // When: 결제 실행
//        PayTicket.Output output = payTicket.execute(input);
//
//        // Then: 결제가 성공했는지 확인
//        assertEquals("COMPLETED", output.status());
//        assertEquals("결제가 완료되었습니다. 잔액: 100", output.message());
//
//        // 트랜잭션 상태 확인
//        Reserve updatedTransaction = ticketTransactionRepository.findById(reserve.getId());
//        assertEquals(TransactionStatus.COMPLETED, updatedTransaction.getTransactionStatus());
//
//        // 티켓 상태 확인
//        Ticket updatedTicket = ticketRepository.findById(ticket.getId());
//        assertEquals(TicketStatus.PAID, updatedTicket.getStatus());
//
//        // 포인트 잔액 확인
//        Point updatedPoint = pointRepository.findById(point.getId());
//        assertEquals(100, updatedPoint.getBalance());
//
//        // 포인트 히스토리 확인
//        PointHistory pointHistory = pointHistoryRepository.findAll().get(0);
//        assertEquals(point.getId(), pointHistory.getPointId());
//        assertEquals(reserve.getAmount(), pointHistory.getAmount());
//        assertEquals("PAY", pointHistory.getType().name());
//    }
//
//    @Test
//    void testExecute_InsufficientBalance() {
//        // Given: 포인트 잔액이 부족한 상황으로 설정
//        point.setBalance(0); // 잔액 부족
//        pointRepository.save(point);
//        PayTicket.Input input = new PayTicket.Input(reserve.getIdempotencyKey(), point.getUserId());
//
//        // When: 결제 실행
//        PayTicket.Output output = payTicket.execute(input);
//
//        // Then: 결제가 실패했는지 확인
//        assertEquals("FAILED", output.status());
//        assertEquals("잔액이 부족합니다.", output.message());
//
//        // 트랜잭션 상태 확인
//        Reserve updatedTransaction = ticketTransactionRepository.findById(reserve.getId());
//        assertEquals(TransactionStatus.FAILED, updatedTransaction.getTransactionStatus());
//    }
//
//    @Test
//    void testExecute_TransactionExpired() {
//        // Given: 트랜잭션이 만료된 상황으로 설정
//        reserve.setExpiredAt(LocalDateTime.now().minusMinutes(1)); // 이미 만료된 트랜잭션
//        reserve = ticketTransactionRepository.save(reserve);
//        PayTicket.Input input = new PayTicket.Input(reserve.getIdempotencyKey(), point.getUserId());
//
//        // When: 결제 실행
//        PayTicket.Output output = payTicket.execute(input);
//
//        // Then: 트랜잭션 상태 확인
//        Reserve updatedTransaction = ticketTransactionRepository.findById(reserve.getId());
//        assertEquals(TransactionStatus.EXPIRED.name(), output.status());
//        assertEquals(TransactionStatus.EXPIRED, updatedTransaction.getTransactionStatus());
//    }
//}
