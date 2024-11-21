package com.chaw.concert.app.domain.concert.reserve.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConcertOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    ConcertOutboxStatus status;

    @Column(name = "type")
    ConcertOutboxType type;

    @Column(name = "concert_schedule_id")
    Long concertScheduleId;

    @Column(name = "ticket_id")
    Long ticketId;

    @Column(name = "user_id")
    Long userId;

    @CreatedDate
    @Column(name = "created_at")
    LocalDateTime createdAt; // "생성일"

    public void published() {
        this.status = ConcertOutboxStatus.PUBLISHED;
    }

    private static ConcertOutbox create(ConcertOutboxType type, Long concertScheduleId, Long ticketId, Long userId) {
        return ConcertOutbox.builder()
                .status(ConcertOutboxStatus.INIT)
                .type(type)
                .concertScheduleId(concertScheduleId)
                .ticketId(ticketId)
                .userId(userId)
                .build();
    }

    public static ConcertOutbox createReserved(Long concertScheduleId, Long ticketId, Long userId) {
        return create(ConcertOutboxType.RESERVED, concertScheduleId, ticketId, userId);
    }

    public static ConcertOutbox createPaid(Long concertScheduleId, Long ticketId, Long userId) {
        return create(ConcertOutboxType.PAID, concertScheduleId, ticketId, userId);
    }
}
