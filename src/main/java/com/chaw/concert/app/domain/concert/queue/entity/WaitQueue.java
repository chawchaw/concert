package com.chaw.concert.app.domain.concert.queue.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class WaitQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id")
    private Long concertId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "uuid")
    private String uuid;

}
