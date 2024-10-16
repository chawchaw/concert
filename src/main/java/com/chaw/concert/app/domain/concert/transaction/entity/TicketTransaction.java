package com.chaw.concert.app.domain.concert.transaction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "ticket_id")
    Long ticketId;

    @Column(name = "point_history_id")
    Long pointHistoryId;

    @Column(name = "idempotency_key")
    String idempotencyKey; // "멱등성 키"

    @Column(name = "transaction_status")
    TransactionStatus transactionStatus; // "트랜잭션 상태 (pending, completed, failed)"

    @Column(name = "payment_method")
    PaymentMethod paymentMethod; // "결제 수단 (카드, 계좌이체)"

    @Column(name = "payment_data")
    String paymentData; // "결제 데이터"

    @Column(name = "amount")
    Integer amount; // "결제 금액"

    @Column(name = "created_at")
    LocalDateTime createdAt; // "생성일"

    @Column(name = "updated_at")
    LocalDateTime updatedAt; // "마지막 업데이트 시간"

    @Column(name = "expired_at")
    LocalDateTime expiredAt; // "만료일"

    @Column(name = "is_deleted")
    Boolean isDeleted; // "삭제 여부"
}
