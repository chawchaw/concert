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
public class Reserve {

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

}
