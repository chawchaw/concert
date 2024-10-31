package com.chaw.concert.app.infrastructure.redis.helper;

import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.MessageFormat;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonRLockAop {
    private static final String REDIS_LOCK_KEY_PREFIX = "rLock:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(RedissonRLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedissonRLock redissonRLock = method.getAnnotation(RedissonRLock.class);
        String lockKey = REDIS_LOCK_KEY_PREFIX + getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                redissonRLock.key());
//        String lockKey = REDIS_LOCK_KEY_PREFIX;
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            boolean lockAcquired = rLock.tryLock(redissonRLock.waitTime(), redissonRLock.leaseTime(), redissonRLock.timeUnit());
            if (!lockAcquired) {
//                log.warn("Redisson RLock 획득 실패: {}", lockKey);
                throw new BaseException(ErrorType.CONFLICT, "Redisson 락 획득 실패");
            }

            return aopForTransaction.proceed(joinPoint);
        } catch (RuntimeException e) {
//            log.warn("Redisson RLock 획득 중 에러 발생: {}: {}", lockKey, e.getMessage());
            throw new BaseException(ErrorType.CONFLICT, MessageFormat.format("Redisson RLock 획득 중 에러 발생: {0}: {1}", lockKey, e.getMessage()));
        } finally {
            rLock.unlock();
        }
    }

    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        try {
            // SpEL 표현식을 평가합니다.
            return parser.parseExpression(key).getValue(context, Object.class);
        } catch (Exception e) {
            // 예외 발생 시 로그 및 기본값 반환
            log.error("SpEL 표현식 파싱 실패: {}, 이유: {}", key, e.getMessage());
            return null; // 혹은 예외를 던지도록 할 수도 있습니다.
        }
    }
}
