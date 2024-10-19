package com.chaw.app.domain.concert.reserve.validation;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.exception.NotEnoughBalanceException;
import com.chaw.concert.app.domain.common.user.exception.PointNotFoundException;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.query.exception.ConcertNotFoundException;
import com.chaw.concert.app.domain.concert.query.exception.ConcertScheduleNotFoundException;
import com.chaw.concert.app.domain.concert.query.exception.TicketAlreadyReservedException;
import com.chaw.concert.app.domain.concert.query.exception.TicketNotFoundException;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.domain.concert.reserve.exception.*;
import com.chaw.concert.app.domain.concert.reserve.validation.ReserveValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReserveValidationUnitTest {

    private ReserveValidation reserveValidation;

    @BeforeEach
    void setUp() {
        reserveValidation = new ReserveValidation();
    }

    @Test
    void validateConcertDetails_ConcertNotFoundException() {
        Concert concert = null;
        ConcertSchedule concertSchedule = new ConcertSchedule();
        Ticket ticket = new Ticket();

        assertThrows(ConcertNotFoundException.class, () -> {
            reserveValidation.validateConcertDetails(concert, concertSchedule, ticket);
        });
    }

    @Test
    void validateConcertDetails_ConcertScheduleNotFoundException() {
        Concert concert = new Concert();
        ConcertSchedule concertSchedule = null;
        Ticket ticket = new Ticket();

        assertThrows(ConcertScheduleNotFoundException.class, () -> {
            reserveValidation.validateConcertDetails(concert, concertSchedule, ticket);
        });
    }

    @Test
    void validateConcertDetails_TicketNotFoundException() {
        Concert concert = new Concert();
        ConcertSchedule concertSchedule = new ConcertSchedule();
        Ticket ticket = null;

        assertThrows(TicketNotFoundException.class, () -> {
            reserveValidation.validateConcertDetails(concert, concertSchedule, ticket);
        });
    }

    @Test
    void validateConcertDetails_IllegalConcertAndScheduleException() {
        Concert concert = new Concert();
        concert.setId(1L);
        ConcertSchedule concertSchedule = new ConcertSchedule();
        concertSchedule.setConcertId(2L);
        Ticket ticket = new Ticket();
        ticket.setConcertScheduleId(2L);

        assertThrows(IllegalConcertAndScheduleException.class, () -> {
            reserveValidation.validateConcertDetails(concert, concertSchedule, ticket);
        });
    }

    @Test
    void validateConcertDetails_IllegalScheduleAndTicketException() {
        Concert concert = new Concert();
        concert.setId(1L);
        ConcertSchedule concertSchedule = new ConcertSchedule();
        concertSchedule.setId(1L);
        concertSchedule.setConcertId(1L);
        Ticket ticket = new Ticket();
        ticket.setConcertScheduleId(2L);

        assertThrows(IllegalScheduleAndTicketException.class, () -> {
            reserveValidation.validateConcertDetails(concert, concertSchedule, ticket);
        });
    }

    @Test
    void validateReserveDetails_TicketAlreadyReservedException() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);  // TicketStatus가 EMPTY가 아님

        assertThrows(TicketAlreadyReservedException.class, () -> {
            reserveValidation.validateReserveDetails(ticket);
        });
    }

    @Test
    void validatePayTicketDetails_PointNotFoundException() {
        Point point = null;  // 포인트 없음
        Reserve reserve = new Reserve();
        Ticket ticket = new Ticket();

        assertThrows(PointNotFoundException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
    }

    @Test
    void validatePayTicketDetails_NotEnoughBalanceException() {
        Point point = new Point();
        point.setBalance(50);  // 잔액 부족
        Reserve reserve = new Reserve();
        reserve.setAmount(100);  // 필요한 금액
        Ticket ticket = new Ticket();

        assertThrows(NotEnoughBalanceException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
    }

    @Test
    void validatePayTicketDetails_TicketNotInStatusReserveException() {
        Point point = new Point();
        point.setBalance(100);
        Reserve reserve = new Reserve();
        reserve.setAmount(50);
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.PAID);  // 티켓이 RESERVE 상태가 아님

        assertThrows(TicketNotInStatusReserveException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
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

        assertThrows(AlreadyPaidReserveException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
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

        assertThrows(CanceledReserveException.class, () -> {
            reserveValidation.validatePayTicketDetails(point, reserve, ticket);
        });
    }
}
