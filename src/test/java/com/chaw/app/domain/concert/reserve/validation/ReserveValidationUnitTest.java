package com.chaw.app.domain.concert.reserve.validation;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
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
        reserveValidation = new
                ReserveValidation();
    }

    @Test
    void validateConcertDetails_콘서트와_일정이_일치하지_않음() {
        Long userId = 0L;
        Concert concert = Concert.builder().id(1L).build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder().concertId(2L).build();

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validateConcertDetails(userId, concert, concertSchedule, null);
        });
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
    }

    @Test
    void validateConcertDetails_일정과_티켓이_일치하지_않음() {
        Long userId = 0L;
        Concert concert = Concert.builder().id(1L).build();
        ConcertSchedule concertSchedule = ConcertSchedule.builder().id(1L).concertId(1L).build();
        Ticket ticket = Ticket.builder().concertScheduleId(2L).build();

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validateConcertDetails(userId, concert, concertSchedule, ticket);
        });
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
    }

    @Test
    void validateReserveDetails_TicketAlreadyReservedException() {
        Ticket ticket = new Ticket();
        ticket.pay();  // TicketStatus가 EMPTY가 아님

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validateReserveDetails(ticket);
        });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }

    @Test
    void validatePayTicketDetails_NotEnoughBalanceException() {
        Point point = Point.builder().balance(50).build();
        Reserve reserve = Reserve.builder().amount(100).build();
        Ticket ticket = new Ticket();

        BaseException baseException = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(0L, point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }

    @Test
    void validatePayTicketDetails_TicketNotInStatusReserveException() {
        Point point = Point.builder().balance(100).build();
        Reserve reserve = Reserve.builder().amount(50).build();
        Ticket ticket = Ticket.builder().id(1L).build();
        ticket.pay();  // 티켓이 RESERVE 상태가 아님

        BaseException baseException = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(0L, point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, baseException.getErrorType());
    }

    @Test
    void validatePayTicketDetails_AlreadyPaidReserveException() {
        Point point = Point.builder().balance(100).build();
        Reserve reserve = Reserve.builder().amount(50).reserveStatus(ReserveStatus.PAID).build();  // 이미 결제 완료됨
        Ticket ticket = new Ticket();
        ticket.reserveWithUserId(0L);

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(0L, point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }

    @Test
    void validatePayTicketDetails_CanceledReserveException() {
        Point point = Point.builder().balance(100).build();
        Reserve reserve = Reserve.builder().amount(50).reserveStatus(ReserveStatus.CANCEL).build();  // 예약이 취소됨
        Ticket ticket = new Ticket();
        ticket.reserveWithUserId(0L);

        BaseException exception = assertThrows(BaseException.class, () -> {
            reserveValidation.validatePayTicketDetails(0L, point, reserve, ticket);
        });
        assertEquals(ErrorType.CONFLICT, exception.getErrorType());
    }
}
