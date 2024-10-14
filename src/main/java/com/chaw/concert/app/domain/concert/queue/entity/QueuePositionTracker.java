package com.chaw.concert.app.domain.concert.queue.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class QueuePositionTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_schedule_id", unique = true)
    private Long concertScheduleId;

    @Column(name = "wait_queue_id")
    private Long waitQueueId;

    @Column(name = "is_wait_queue_exist")
    private Boolean isWaitQueueExist;
}
