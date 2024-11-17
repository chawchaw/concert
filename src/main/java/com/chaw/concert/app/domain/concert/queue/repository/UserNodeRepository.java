package com.chaw.concert.app.domain.concert.queue.repository;

import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;

import java.util.List;

public interface UserNodeRepository {

    UserNode findByUserId(Long userId); // 대기열 순서 조회

    List<UserNode> findTopWait(int size); // 상위 조회

    UserNode createWait(Long userId); // 대기열 입장

    UserNode createActive(Long userId); // 대기열 입장

    void activeUserNodes(List<UserNode> userNodes); // 상태 변경

    long countByStatus(UserNodeStatus status);
}
