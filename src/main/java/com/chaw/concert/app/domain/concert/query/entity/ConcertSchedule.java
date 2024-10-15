package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id")
    private Long concertId; // 공연ID

    @Column(name = "hall_id")
    private Long hallId; // 공연장ID

    @Column(name = "is_sold")
    private Boolean isSold; // 판매 여부

    @Column(name = "total_seat")
    private Integer totalSeat; // 총 좌석 수

    @Column(name = "available_seat")
    private Integer availableSeat; // 남은 좌석 수

    @Column(name = "date_concert")
    private Integer dateConcert; // 공연일
}
