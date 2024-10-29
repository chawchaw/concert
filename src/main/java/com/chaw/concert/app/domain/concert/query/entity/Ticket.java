package com.chaw.concert.app.domain.concert.query.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ticket {

    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_schedule_id")
    private Long concertScheduleId; // 공연일정 ID

    @Column
    @Convert(converter = TicketTypeConverter.class)
    private TicketType type;

    @Column
    @Convert(converter = TicketStatusConverter.class)
    private TicketStatus status;

    @Column
    private Integer price; // 가격

    @Column(name = "seat_no")
    private String seatNo; // 좌석 번호

    @Column(name = "reserve_user_id")
    private Long reserveUserId; // "예약 사용자"

    public void resetToEmpty() {
        this.status = TicketStatus.EMPTY;
        this.reserveUserId = null;
    }

    public void pay() {
        this.status = TicketStatus.PAID;
    }

    public void reserveWithUserId(Long userId) {
        this.status = TicketStatus.RESERVE;
        this.reserveUserId = userId;
    }

    public BaseException getExceptionForNotReservable() {
        return new BaseException(ErrorType.CONFLICT, "이미 예약이 완료된 티켓입니다.");
    }

    public void isReservableOrThrow() {
        if (!this.status.equals(TicketStatus.EMPTY)) {
            throw getExceptionForNotReservable();
        }
    }

    public void isPayableOrThrow() {
        if (!this.status.equals(TicketStatus.RESERVE)) {
            throw new BaseException(ErrorType.CONFLICT, "예약된 티켓이 아닙니다.");
        }
    }
}
