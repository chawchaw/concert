package com.chaw.concert.app.domain.concert.reserve.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "concert_schedule_id")
    private Long concertScheduleId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod; // "결제 수단 (카드, 계좌이체)"

    @Column(name = "amount")
    private Integer amount; // "결제 금액"

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt; // "생성일"

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
