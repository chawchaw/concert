package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.*;
import org.springframework.data.geo.Point;

@Entity
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 127)
    private String name; // 공연장 이름

    @Column(length = 127)
    private String address; // 주소

    @Column(length = 127)
    private String address_detail; // 상세주소

    @Column(length = 127)
    private Point point; // 위/경도
}
