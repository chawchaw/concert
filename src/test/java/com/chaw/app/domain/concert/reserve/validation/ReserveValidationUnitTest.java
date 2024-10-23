package com.chaw.app.domain.concert.reserve.validation;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.validation.ReserveValidation;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReserveValidationUnitTest {

    private ReserveValidation reserveValidation;

    @BeforeEach
    void setUp() {
        reserveValidation = new ReserveValidation();
    }

    @Test
    void validateReserveDetails_TicketAlreadyReservedException() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);  // TicketStatus가 EMPTY가 아님

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validateReserveDetails(ticket);
        });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }

    @Test
    void validatePayTicketDetails_NotEnoughBalanceException() {
        Point point = new Point();
        point.setBalance(50);  // 잔액 부족
        Reserve reserve = new Reserve();
        reserve.setAmount(100);  // 필요한 금액
        Ticket ticket = new Ticket();

        BaseException baseException = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }

    @Test
    void validatePayTicketDetails_TicketNotInStatusReserveException() {
        Point point = new Point();
        point.setBalance(100);
        Reserve reserve = new Reserve();
        reserve.setAmount(50);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);  // 티켓이 RESERVE 상태가 아님

        BaseException baseException = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }

    @Test
    void validatePayTicketDetails_AlreadyPaidReserveException() {
        Point point = new Point();
        point.setBalance(100);
        Reserve reserve = new Reserve();
        reserve.setAmount(50);
        reserve.setReserveStatus(ReserveStatus.PAID);  // 예약이 이미 결제 완료됨
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }

    @Test
    void validatePayTicketDetails_CanceledReserveException() {
        Point point = new Point();
        point.setBalance(100);
        Reserve reserve = new Reserve();
        reserve.setAmount(50);
        reserve.setReserveStatus(ReserveStatus.CANCEL);  // 예약이 취소됨
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESERVE);

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }
}
