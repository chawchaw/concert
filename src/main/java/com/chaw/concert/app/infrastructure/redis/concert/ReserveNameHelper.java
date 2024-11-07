package com.chaw.concert.app.infrastructure.redis.concert;

import org.springframework.stereotype.Component;

@Component
public class ReserveNameHelper {

    private static final String KEY = "reserve";

    public String getName(Long concertScheduleId, Long ticketId, Long userId) {
        return KEY + ":" + concertScheduleId + ":" + ticketId + ":" + userId;
    }

}
