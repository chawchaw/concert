package com.chaw.concert.app.domain.concert.queue.repository;

import com.chaw.concert.app.domain.concert.queue.entity.WaitToken;

import java.util.List;

public interface WaitTokenRepository {

    Integer getRankByUserId(Long userId); // 대기열 순서 조회

    boolean saveWithCurrentTime(Long userId); // 대기열 저장

    List<WaitToken> getTokensEligibleForPass(int endIndex);

    Integer removeTokensBeforeTimeStamp(Double timeStamp); // 특정 시간 이전 토큰 삭제

    int countAll();
}
