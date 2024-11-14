package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ticket implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_schedule_id")
    private Long concertScheduleId; // 공연일정 ID

    @Column
    @Convert(converter = TicketTypeConverter.class)
    private TicketType type;

    @Column
    private Integer price; // 가격

    @Column(name = "seat_no")
    private String seatNo; // 좌석 번호

}
