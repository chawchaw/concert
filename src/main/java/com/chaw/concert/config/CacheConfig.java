package com.chaw.concert.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Duration DEFAULT = Duration.ofMinutes(5);
    private static final Duration GET_TICKETS = Duration.ofHours(24);
    private static final Duration GET_TICKETS_IN_EMPTY = Duration.ofSeconds(1);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT);

        // 캐시 이름별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("tickets",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(GET_TICKETS));

        cacheConfigurations.put("ticketsInEmpty",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(GET_TICKETS_IN_EMPTY));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
