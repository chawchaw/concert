package com.chaw.concert.app.domain.common.user.entity;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
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

    public final static String REDIS_LOCK_KEY = "'user-point:'.concat(#input.userId().toString())";

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

    public boolean hasEnoughBalance(Integer amount) {
        return this.balance >= amount;
    }

    public void validateHasEnoughBalanceOrThrow(Integer amount) {
        if (!hasEnoughBalance(amount)) {
            throw new BaseException(ErrorType.CONFLICT, "잔액이 부족합니다.");
        }
    }
}
