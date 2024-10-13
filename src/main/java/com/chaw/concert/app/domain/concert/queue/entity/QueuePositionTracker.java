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

    @Column(name = "concert_id", unique = true)
    private Long concertId;

    @Column(name = "waiting_user_id")
    private Long waitingUserId;

    @Column(name = "is_wait_queue_exist")
    private Boolean isWaitQueueExist;
}
