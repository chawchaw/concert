package com.chaw.concert.app.domain.concert.queue.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_schedule_id")
    private Long concertScheduleId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "uuid")
    private String uuid;


    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
}
