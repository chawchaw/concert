//package com.chaw.app.domain.concert.reserve.usecase;
//
//import com.chaw.concert.app.domain.common.user.entity.Point;
//import com.chaw.concert.app.domain.common.user.entity.PointHistory;
//import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
//import com.chaw.concert.app.domain.common.user.repository.PointRepository;
//import com.chaw.concert.app.domain.concert.query.entity.Ticket;
//import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
//import com.chaw.concert.app.domain.concert.query.repository.TicketRepository;
//import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
//import com.chaw.concert.app.domain.concert.reserve.entity.TransactionStatus;
//import com.chaw.concert.app.domain.concert.reserve.exception.TicketNotInStatusReserve;
//import com.chaw.concert.app.domain.concert.reserve.repository.TicketTransactionRepository;
//import com.chaw.concert.app.domain.concert.reserve.usecase.PayTicket;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class PayTicketUnitTest {
//
//    @Mock
//    private TicketTransactionRepository ticketTransactionRepository;
//
//    @Mock
//    private PointHistoryRepository pointHistoryRepository;
//
//    @Mock
//    private PointRepository pointRepository;
//
//    @Mock
//    private TicketRepository ticketRepository;
//
//    @InjectMocks
//    private PayTicket payTicket;
//
//    private Reserve reserve;
//    private Ticket ticket;
//    private Point point;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // 트랜잭션 생성
//        reserve = new Reserve();
//        reserve.setIdempotencyKey("testKey");
//        reserve.setExpiredAt(LocalDateTime.now().plusMinutes(10)); // 만료되지 않음
//        reserve.setTransactionStatus(TransactionStatus.PENDING);
//        reserve.setAmount(5000);
//        reserve.setUserId(1L);
//        reserve.setTicketId(1L);
//
//        // 티켓 생성
//        ticket = new Ticket();
//        ticket.setId(1L);
//        ticket.setStatus(TicketStatus.RESERVE);
//
//        // 포인트 생성
//        point = new Point();
//        point.setUserId(1L);
//        point.setBalance(10000); // 잔액 충분
//    }
//
//    @Test
//    void testExecute_SuccessfulPayment() {
//        // Given
//        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock("testKey", false))
//                .thenReturn(reserve);
//        when(ticketRepository.findById(1L)).thenReturn(ticket);
//        when(pointRepository.findByUserIdWithLock(1L)).thenReturn(point);
//
//        // When
//        PayTicket.Input input = new PayTicket.Input("testKey", 1L);
//        PayTicket.Output output = payTicket.execute(input);
//
//        // Then
//        assertEquals("COMPLETED", output.status());
//        assertEquals("결제가 완료되었습니다. 잔액: 5000", output.message());
//        assertEquals(TransactionStatus.COMPLETED, reserve.getTransactionStatus());
//        assertEquals(TicketStatus.PAID, ticket.getStatus());
//        verify(ticketTransactionRepository, times(1)).save(reserve);
//        verify(ticketRepository, times(1)).save(ticket);
//        verify(pointRepository, times(1)).save(point);
//        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
//    }
//
//    @Test
//    void testExecute_TransactionExpired() {
//        // Given: 만료된 트랜잭션
//        reserve.setExpiredAt(LocalDateTime.now().minusMinutes(1)); // 이미 만료됨
//        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock("testKey", false))
//                .thenReturn(reserve);
//
//        // When
//        PayTicket.Input input = new PayTicket.Input("testKey", 1L);
//        PayTicket.Output output = payTicket.execute(input);
//
//        // Then
//        verify(ticketTransactionRepository, times(1)).save(reserve);
//        assertEquals(TransactionStatus.EXPIRED, reserve.getTransactionStatus());
//    }
//
//    @Test
//    void testExecute_TicketNotInReserveStatus() {
//        // Given: 티켓이 RESERVE 상태가 아님
//        ticket.setStatus(TicketStatus.PAID);
//        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock("testKey", false))
//                .thenReturn(reserve);
//        when(ticketRepository.findById(1L)).thenReturn(ticket);
//
//        // When
//        PayTicket.Input input = new PayTicket.Input("testKey", 1L);
//
//        // Then
//        assertThrows(TicketNotInStatusReserve.class, () -> payTicket.execute(input));
//    }
//
//    @Test
//    void testExecute_InsufficientBalance() {
//        // Given: 잔액 부족
//        point.setBalance(1000); // 잔액 부족
//        when(ticketTransactionRepository.findByIdempotencyKeyAndIsDeletedWithLock("testKey", false))
//                .thenReturn(reserve);
//        when(ticketRepository.findById(1L)).thenReturn(ticket);
//        when(pointRepository.findByUserIdWithLock(1L)).thenReturn(point);
//
//        // When
//        PayTicket.Input input = new PayTicket.Input("testKey", 1L);
//        PayTicket.Output output = payTicket.execute(input);
//
//        // Then
//        assertEquals("FAILED", output.status());
//        assertEquals("잔액이 부족합니다.", output.message());
//        assertEquals(TransactionStatus.FAILED, reserve.getTransactionStatus());
//        verify(ticketTransactionRepository, times(1)).save(reserve);
//    }
//}
