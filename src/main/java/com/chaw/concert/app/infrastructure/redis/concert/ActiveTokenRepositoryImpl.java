package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.queue.repository.ActiveTokenRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@AllArgsConstructor
@Repository
public class ActiveTokenRepositoryImpl implements ActiveTokenRepository {

    private final RedissonClient redissonClient;
    private final ActiveTokenNameHelper activeTokenNameHelper;

    @Override
    public Boolean existsByUserId(Long userId) {
        String name = activeTokenNameHelper.getName(userId);
        return redissonClient.getBucket(name).isExists();
    }

    @Override
    public void save(Long userId, Integer timeToLiveSeconds) {
        String name = activeTokenNameHelper.getName(userId);
        redissonClient.getBucket(name).set(true);
        redissonClient.getBucket(name).expire(timeToLiveSeconds, TimeUnit.SECONDS);
    }

    @Override
    public long countAll() {
        RKeys keys = redissonClient.getKeys();
        return StreamSupport.stream(keys.getKeysByPattern(activeTokenNameHelper.getPatten()).spliterator(), false).count();
    }
}
