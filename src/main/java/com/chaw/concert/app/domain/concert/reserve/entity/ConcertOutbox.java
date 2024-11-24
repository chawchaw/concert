package com.chaw.concert.app.domain.concert.reserve.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.concert.app.infrastructure.kafka.KafkaTopics;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConcertOutbox {

    public static final int RETRY_LIMIT = 3;
    public static final int RETRY_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ConcertOutboxStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ConcertOutboxType type;

    @Column(name = "concert_schedule_id")
    private Long concertScheduleId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "retry_count")
    private int retryCount;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt; // "생성일"

    @Column(name = "last_retried_at")
    private LocalDateTime lastRetriedAt; // "재시도 일시"

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // "수정일"

    private static ConcertOutbox create(ConcertOutboxType type, Long concertScheduleId, Long ticketId, Long userId) {
        return ConcertOutbox.builder()
                .status(ConcertOutboxStatus.INIT)
                .type(type)
                .concertScheduleId(concertScheduleId)
                .ticketId(ticketId)
                .userId(userId)
                .retryCount(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static ConcertOutbox createReserved(Long concertScheduleId, Long ticketId, Long userId) {
        return create(ConcertOutboxType.RESERVED, concertScheduleId, ticketId, userId);
    }

    public static ConcertOutbox createPaid(Long concertScheduleId, Long ticketId, Long userId) {
        return create(ConcertOutboxType.PAID, concertScheduleId, ticketId, userId);
    }

    public static List<ConcertOutboxStatus> getRetryableStatuses() {
        return List.of(ConcertOutboxStatus.INIT, ConcertOutboxStatus.RETRY);
    }

    public static LocalDateTime getRetryableBefore() {
        return LocalDateTime.now().minusMinutes(RETRY_MINUTES);
    }

    public void published() {
        this.status = ConcertOutboxStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    public void retried() {
        this.status = ConcertOutboxStatus.RETRY;
        this.lastRetriedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.retryCount++;
    }

    public void failed() {
        this.status = ConcertOutboxStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRetryable() {
        return this.retryCount < RETRY_LIMIT
                && (this.status == ConcertOutboxStatus.INIT || this.status == ConcertOutboxStatus.RETRY);
    }

    public String toMessageForRetryFailed() {
        return String.format("Kafka 발행 실패: 시도횟수=%d, concertOutboxId=%d, type=%s", retryCount, id, type.getDbValue());
    }

    public String getTopic() {
        switch (type) {
            case RESERVED:
                return KafkaTopics.CONCERT_RESERVE_TOPIC_DATASTORE;
            case PAID:
                return KafkaTopics.CONCERT_PAY_TOPIC_DATASTORE;
            default:
                throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION, "ConcertOutbox type 이 잘못되었습니다.");
        }
    }
}
