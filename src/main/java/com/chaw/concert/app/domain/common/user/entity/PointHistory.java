package com.chaw.concert.app.domain.common.user.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column
    @Convert(converter = PointHistoryTypeConverter.class)
    private PointHistoryType type; // 변경 타입

    @Column
    private Integer amount; // 변경 금액

    @Column(name = "date_transaction")
    private LocalDateTime dateTransaction; // 변경일
}
