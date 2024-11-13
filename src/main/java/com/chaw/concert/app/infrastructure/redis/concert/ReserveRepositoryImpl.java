package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@AllArgsConstructor
@Repository
public class ReserveRepositoryImpl implements ReserveRepository {

    private final RedissonClient redissonClient;
    private final ReserveNameHelper reserveNameHelper;

    @Override
    public Boolean existsByConcertScheduleIdAndTicketIdAndUserId(Long concertScheduleId, Long ticketId, Long userId) {
        String name = reserveNameHelper.getName(concertScheduleId, ticketId, userId);
        return redissonClient.getBucket(name).isExists();
    }

    @Override
    public void save(Reserve reserve) {
        String name = reserveNameHelper.getName(reserve.concertScheduleId(), reserve.ticketId(), reserve.userId());
        redissonClient.getBucket(name).set(true);
        redissonClient.getBucket(name).expire(Reserve.RESERVE_LIMIT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public Set<Long> findByConcertScheduleId(Long concertScheduleId) {
        Iterable<String> keysIterable = redissonClient
                .getKeys()
                .getKeysByPattern(reserveNameHelper.getPattern(concertScheduleId));

        return StreamSupport.stream(keysIterable.spliterator(), false)
                .map(key -> reserveNameHelper.getTicketId(key))
                .collect(Collectors.toSet());
    }
}
