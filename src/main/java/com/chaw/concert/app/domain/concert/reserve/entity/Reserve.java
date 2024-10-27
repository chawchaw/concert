package com.chaw.concert.app.domain.concert.reserve.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reserve {

    public final static Integer EXPIRED_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "ticket_id")
    Long ticketId;

    @Column(name = "status")
    ReserveStatus reserveStatus; // "상태 (reserve, paid, canceled)"

    @Column(name = "amount")
    Integer amount; // "결제 금액"

    @Column(name = "created_at")
    LocalDateTime createdAt; // "생성일"

    @Column(name = "updated_at")
    LocalDateTime updatedAt; // "마지막 업데이트 시간"

    public void cancel() {
        this.reserveStatus = ReserveStatus.CANCEL;
        this.updatedAt = LocalDateTime.now();
    }

    public void pay() {
        this.reserveStatus = ReserveStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCreationTimeToPast(int minutes) {
        this.createdAt = LocalDateTime.now().minusMinutes(minutes);
    }

    public static LocalDateTime getExpiredTimeFromNow() {
        return LocalDateTime.now().minusMinutes(EXPIRED_MINUTES);
    }

    public void isReservableStatusOrThrow() {
        if (this.reserveStatus == ReserveStatus.PAID) {
            throw new BaseException(ErrorType.CONFLICT, "결제 완료된 예약입니다.");
        }
        else if (this.reserveStatus == ReserveStatus.CANCEL) {
            throw new BaseException(ErrorType.CONFLICT, "취소된 예약입니다.");
        }
    }

    public void isExpiredThenDoAndThrow(Runnable runnable) {
        if (LocalDateTime.now().isAfter(this.createdAt.plusMinutes(EXPIRED_MINUTES))) {
            runnable.run();
            throw new BaseException(ErrorType.CONFLICT, "결제 유효기간이 만료되었습니다.");
        }
    }

    public static Reserve create(Long userId, Long ticketId, Integer amount) {
        return Reserve.builder()
                .userId(userId)
                .ticketId(ticketId)
                .reserveStatus(ReserveStatus.RESERVE)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
