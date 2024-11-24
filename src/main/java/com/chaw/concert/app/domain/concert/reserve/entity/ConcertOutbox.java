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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 활성화
public class ConcertOutbox {

    private static final ConcertOutboxStatus DEFAULT_STATUS = ConcertOutboxStatus.INIT;
    private static final int DEFAULT_RETRY_COUNT = 0;

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

    @Builder.Default
    @Column(name = "retry_count")
    private int retryCount = DEFAULT_RETRY_COUNT;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt; // "생성일"

    @Column(name = "last_retried_at")
    private LocalDateTime lastRetriedAt; // "재시도 일시"

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // "수정일"

    private static ConcertOutbox create(ConcertOutboxType type, Long concertScheduleId, Long ticketId, Long userId) {
        return ConcertOutbox.builder()
                .status(DEFAULT_STATUS)
                .type(type)
                .concertScheduleId(concertScheduleId)
                .ticketId(ticketId)
                .userId(userId)
                .retryCount(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void updateStatus(ConcertOutboxStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
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
        updateStatus(ConcertOutboxStatus.PUBLISHED);
    }

    public void retried() {
        updateStatus(ConcertOutboxStatus.RETRY);
        this.updatedAt = LocalDateTime.now();
        this.retryCount++;
    }

    public void failed() {
        updateStatus(ConcertOutboxStatus.FAILED);
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
                throw new BaseException(ErrorType.DATA_INTEGRITY_VIOLATION,
                        String.format("ConcertOutbox type 이 잘못되었습니다. type=%s, concertOutboxId=%d", type, id));
        }
    }
}
