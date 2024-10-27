package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 127)
    private String name; // 공연이름

    @Column(columnDefinition = "TEXT")
    private String info; // 공연정보

    @Column(length = 127)
    private String artist; // 공연자

    @Column(length = 127)
    private String host; // 주최자
}
