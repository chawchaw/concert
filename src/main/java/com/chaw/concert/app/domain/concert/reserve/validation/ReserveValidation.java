package com.chaw.concert.app.domain.concert.reserve.validation;

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
import org.springframework.stereotype.Component;

@Component
public class ReserveValidation {

    public void validateConcertDetails(Concert concert, ConcertSchedule concertSchedule, Ticket ticket) {
        if (concert == null) {
            throw new ConcertNotFoundException();
        }
        if (concertSchedule == null) {
            throw new ConcertScheduleNotFoundException();
        }
        if (ticket == null) {
            throw new TicketNotFoundException();
        }

        if (!concert.getId().equals(concertSchedule.getConcertId())) {
            throw new IllegalConcertAndScheduleException();
        }
        if (!concertSchedule.getId().equals(ticket.getConcertScheduleId())) {
            throw new IllegalScheduleAndTicketException();
        }
    }

    public void validateReserveDetails(Ticket ticket) {
        if (!ticket.getStatus().equals(TicketStatus.EMPTY)) {
            throw new TicketAlreadyReservedException();
        }
    }

    /**
     * point 잔액
     * ticket 예약 상태
     * reserve 예약 상태, 예약제한시간
     */
    public void validatePayTicketDetails(Point point, Reserve reserve, Ticket ticket) {
        if (point == null) {
            throw new PointNotFoundException();
        }
        if (reserve == null) {
            throw new ReserveNotFoundException();
        }

        // 잔액 체크
        if (point.getBalance() < reserve.getAmount()) {
            throw new NotEnoughBalanceException();
        }

        // 티켓 예약 상태 체크
        if (ticket.getStatus() != TicketStatus.RESERVE) {
            throw new TicketNotInStatusReserveException();
        }

        // 예약 상태 체크
        if (reserve.getReserveStatus() == ReserveStatus.PAID) {
            throw new AlreadyPaidReserveException();
        }
        else if (reserve.getReserveStatus() == ReserveStatus.CANCEL) {
            throw new CanceledReserveException();
        }
    }
}
