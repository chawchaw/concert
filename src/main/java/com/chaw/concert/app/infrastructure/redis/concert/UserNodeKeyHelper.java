package com.chaw.concert.app.infrastructure.redis.concert;

import org.springframework.stereotype.Component;

@Component
public class UserNodeKeyHelper {

    private static final String KEY_ACTIVE = "active_token";
    private static final String KEY_WAIT = "wait_token";

    public String getActiveKey(Long userId) {
        return KEY_ACTIVE + ":" + userId;
    }

    public String getActiveKeyPattern() {
        return KEY_ACTIVE + ":*";
    }

    public String getWaitKey() {
        return KEY_WAIT;
    }
}
