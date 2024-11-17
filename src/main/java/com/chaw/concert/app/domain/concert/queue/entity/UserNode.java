package com.chaw.concert.app.domain.concert.queue.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserNode {

    public static final int PASS_SIZE = 30;
    public static final int TTL_ACTIVE_TOKEN_SECONDS = 60 * 10;

    Long userId;
    Integer rank;
    Double score;
    UserNodeStatus status;

    public boolean isActive() {
        return this.status == UserNodeStatus.ACTIVE;
    }
}
