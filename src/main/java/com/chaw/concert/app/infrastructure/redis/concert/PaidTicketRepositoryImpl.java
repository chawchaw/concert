package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.reserve.repository.PaidTicketRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class PaidTicketRepositoryImpl implements PaidTicketRepository {

    private final RedissonClient redissonClient;
    private final PaidTicketNameHelper paidTicketNameHelper;

    private RSet<Long> getSet(Long concertScheduleId) {
        String key = paidTicketNameHelper.getName(concertScheduleId);
        return redissonClient.getSet(key);
    }

    @Override
    public void save(Long concertScheduleId, Long ticketId) {
        RSet<Long> set = getSet(concertScheduleId);
        set.add(ticketId);
    }

    @Override
    public int countByConcertScheduleId(Long concertScheduleId) {
        RSet<Long> set = getSet(concertScheduleId);
        return set.size();
    }
}
