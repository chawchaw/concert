package com.chaw.concert.app.domain.concert.reserve.validation;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.concert.query.entity.Concert;
import com.chaw.concert.app.domain.concert.query.entity.ConcertSchedule;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.infrastructure.exception.BaseException;
import com.chaw.concert.app.infrastructure.exception.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class ReserveValidation {

    public void validateConcertDetails(Concert concert, ConcertSchedule concertSchedule, Ticket ticket) {
        if (!concert.getId().equals(concertSchedule.getConcertId())) {
            throw new BaseException(ErrorType.BAD_REQUEST, "concert and schedule not matched");
        }
        if (!concertSchedule.getId().equals(ticket.getConcertScheduleId())) {
            throw new BaseException(ErrorType.BAD_REQUEST, "schedule and ticket not matched");
        }
    }

    public void validateReserveDetails(Ticket ticket) {
        if (!ticket.getStatus().equals(TicketStatus.EMPTY)) {
            throw new BaseException(ErrorType.CONFLICT, "예약할 수 없는 티켓입니다.");
        }
    }

    /**
     * point 잔액
     * ticket 예약 상태
     * reserve 예약 상태, 예약제한시간
     */
    public void validatePayTicketDetails(Point point, Reserve reserve, Ticket ticket) {
        // 잔액 체크
        if (point.getBalance() < reserve.getAmount()) {
            throw new BaseException(ErrorType.CONFLICT, "잔액이 부족합니다.");
        }

        // 티켓 예약 상태 체크
        if (ticket.getStatus() != TicketStatus.RESERVE) {
            throw new BaseException(ErrorType.CONFLICT, "예약된 티켓이 아닙니다.");
        }

        // 예약 상태 체크
        if (reserve.getReserveStatus() == ReserveStatus.PAID) {
            throw new BaseException(ErrorType.CONFLICT, "결제 완료된 예약입니다.");
        }
        else if (reserve.getReserveStatus() == ReserveStatus.CANCEL) {
            throw new BaseException(ErrorType.CONFLICT, "취소된 예약입니다.");
        }
    }
}
