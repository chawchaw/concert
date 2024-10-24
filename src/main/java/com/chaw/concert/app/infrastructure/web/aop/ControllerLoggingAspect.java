package com.chaw.concert.app.infrastructure.web.aop;

import com.chaw.concert.app.domain.common.auth.util.SecurityUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class ControllerLoggingAspect {

    private final SecurityUtil securityUtils;

    public ControllerLoggingAspect(SecurityUtil securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController com.chaw.concert.app.presenter..*)")
    public void restControllerMethods() {}

    @Around("restControllerMethods()")
    public Object addMdc(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId;

        try {
            userId = securityUtils.getCurrentUserId().toString();
        } catch (Exception e) {
            userId = null;
        }

        MDC.put("userId", userId);
        MDC.put("eventId", UUID.randomUUID().toString());

        try {
            return joinPoint.proceed();
        } finally {
            MDC.clear();
        }
    }
}
