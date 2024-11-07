package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.reserve.entity.Reserve;
import com.chaw.concert.app.domain.concert.reserve.repository.ReserveRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

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
}
