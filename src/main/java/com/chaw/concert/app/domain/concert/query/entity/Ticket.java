package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

    @Column(name = "temp_reserve_user_id")
    private Long tempReserveUserId; // "임시예약 사용자"

    @Column(name = "temp_reserve_end_at")
    private LocalDateTime tempReserveEndAt; // "임시예약 마감일"
}
