package com.chaw.concert.app.infrastructure.redis.concert;

import org.springframework.stereotype.Component;

@Component
public class PaidTicketNameHelper {

    private static final String KEY = "paid_ticket";

    public String getName(Long concertScheduleId) {
        return KEY + ":" + concertScheduleId;
    }
}
