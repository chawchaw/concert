package com.chaw.concert.app.domain.common.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column
    @Convert(converter = PointHistoryTypeConverter.class)
    private PointHistoryType type; // 변경 타입

    @Column
    private Integer amount; // 변경 금액

    @Column(name = "date_transaction")
    private LocalDateTime dateTransaction; // 변경일

    public Integer getChangedAmount() {
        if (type == PointHistoryType.PAY) {
            return -amount;
        } else if (type == PointHistoryType.CHARGE) {
            return amount;
        }
        return 0;
    }
}
