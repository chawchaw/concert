package com.chaw.concert.app.domain.concert.reserve.validation;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.concert.query.entity.Ticket;
import com.chaw.concert.app.domain.concert.query.entity.TicketStatus;
import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.entity.ReserveStatus;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReserveValidation {

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

        // 예약 상태 체크
        if (reserve.getReserveStatus() == ReserveStatus.PAID) {
            throw new BaseException(ErrorType.CONFLICT, "결제 완료된 예약입니다.");
        }
        else if (reserve.getReserveStatus() == ReserveStatus.CANCEL) {
            throw new BaseException(ErrorType.CONFLICT, "취소된 예약입니다.");
        }

        // 티켓 예약 상태 체크
        if (ticket.getStatus() != TicketStatus.RESERVE) {
            log.warn("예약되지 않은 티켓({})을 결제시도", ticket.getId());
            throw new BaseException(ErrorType.CONFLICT, "예약된 티켓이 아닙니다.");
        }
    }
}
