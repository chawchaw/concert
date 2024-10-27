package com.chaw.concert.app.domain.common.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance")
    private Integer balance;

    public void increaseBalance(Integer amount) {
        this.balance += amount;
    }

    public void decreaseBalance(Integer amount) {
        this.balance -= amount;
    }
}
