package com.chaw.concert.app.infrastructure.web;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

@Aspect
@Component
public class ControllerLoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController com.chaw.concert.app.presenter..*)")
    public void restControllerMethods() {}

    @Around("restControllerMethods()")
    public Object addMdc(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long userId = null;

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        // @RequestAttribute("userId")가 붙은 파라미터에서 userId를 추출
        for (int i = 0; i < parameters.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof RequestAttribute) {
                    RequestAttribute requestAttribute = (RequestAttribute) annotation;
                    if ("userId".equals(requestAttribute.value())) {
                        userId = (Long) args[i];  // @RequestAttribute("userId")에 해당하는 인자 추출
                        break;
                    }
                }
            }
        }

        MDC.put("userId", userId.toString());
        MDC.put("eventId", UUID.randomUUID().toString());

        try {
            return joinPoint.proceed();
        } finally {
            MDC.clear();
        }
    }
}
