package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.*;

@Entity
public class Ticket {

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
}
