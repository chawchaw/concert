package com.chaw.concert.app.domain.concert.reserve.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "concert_schedule_id")
    Long concertScheduleId;

    @Column(name = "ticket_id")
    Long ticketId;

    @Column(name = "point_history_id")
    Long pointHistoryId;

    @Column(name = "payment_method")
    PaymentMethod paymentMethod; // "결제 수단 (카드, 계좌이체)"

    @Column(name = "amount")
    Integer amount; // "결제 금액"

    @CreatedDate
    @Column(name = "created_at")
    LocalDateTime createdAt; // "생성일"

    public static Payment create(Long userId, Long concertScheduleId, Long ticketId, Long pointHistoryId, PaymentMethod paymentMethod, Integer amount) {
        return Payment.builder()
                .userId(userId)
                .concertScheduleId(concertScheduleId)
                .ticketId(ticketId)
                .pointHistoryId(pointHistoryId)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .build();
    }
}
