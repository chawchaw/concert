package com.chaw.concert.app.infrastructure.redis.helper;

import jakarta.transaction.Transactional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class AopForTransaction {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
