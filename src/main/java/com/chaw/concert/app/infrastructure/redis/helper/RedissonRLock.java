package com.chaw.concert.app.infrastructure.redis.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonRLock {
    String key();

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long waitTime() default 1L;

    long leaseTime() default 1L;
}
