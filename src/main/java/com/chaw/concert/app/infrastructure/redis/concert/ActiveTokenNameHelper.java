package com.chaw.concert.app.infrastructure.redis.concert;

import org.springframework.stereotype.Component;

@Component
public class ActiveTokenNameHelper {

    private static final String KEY = "active_token";

    public String getName(Long userId) {
        return KEY + ":" + userId;
    }
}
