package com.chaw.concert.app.domain.common.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_id")
    private Long pointId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PointHistoryType type; // 변경 타입

    @Column(name = "amount")
    private Integer amount; // 변경 금액

    @CreatedDate
    @Column(name = "date_transaction")
    private LocalDateTime dateTransaction; // 변경일

    public static PointHistory createCharge(Long pointId, Integer amount) {
        return PointHistory.builder()
                .pointId(pointId)
                .type(PointHistoryType.CHARGE)
                .amount(amount)
                .dateTransaction(LocalDateTime.now())
                .build();
    }

    public static PointHistory createPay(Long pointId, Long ticketId, Integer amount) {
        return PointHistory.builder()
                .pointId(pointId)
                .ticketId(ticketId)
                .type(PointHistoryType.PAY)
                .amount(amount)
                .build();
    }
}
