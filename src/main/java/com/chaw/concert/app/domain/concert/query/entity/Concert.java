package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Concert {

    @Id
    private Long id;

    @Column
    private Long hall_id; // 공연장소 ID

    @Column(length = 127)
    private String name; // 공연이름

    @Column(columnDefinition = "TEXT")
    private String info; // 공연정보

    @Column(length = 127)
    private String artist; // 공연자

    @Column(length = 127)
    private String host; // 주최자

    @Column(name = "concert_date")
    private LocalDateTime concertDate; // 공연일

    @Column(name = "can_buy_from")
    private LocalDateTime canBuyFrom; // 예매 가능 시작일

    @Column(name = "can_buy_to")
    private LocalDateTime canBuyTo; // 예매 가능 종료일
}
