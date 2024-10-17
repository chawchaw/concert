package com.chaw.concert.app.domain.concert.reserve.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "reserve_id")
    Long reserveId;

    @Column(name = "point_history_id")
    Long pointHistoryId;

    @Column(name = "payment_method")
    PaymentMethod paymentMethod; // "결제 수단 (카드, 계좌이체)"

    @Column(name = "amount")
    Integer amount; // "결제 금액"

    @Column(name = "created_at")
    LocalDateTime createdAt; // "생성일"

}
