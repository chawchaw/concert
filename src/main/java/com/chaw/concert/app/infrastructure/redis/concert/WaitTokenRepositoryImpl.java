package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.queue.repository.WaitTokenRepository;
import lombok.AllArgsConstructor;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Repository
public class WaitTokenRepositoryImpl implements WaitTokenRepository {

    private static final String KEY = "wait_token";
    private static final int PASS_SIZE = 30;

    private final RedissonClient redissonClient;

    @Override
    public Integer getRankByUserId(Long userId) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(KEY);
        return sortedSet.rank(userId);
    }

    @Override
    public boolean saveWithCurrentTime(Long userId) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(KEY);
        long score = System.currentTimeMillis();
        return sortedSet.add(score, userId);
    }

    @Override
    public List<Long> getTokensEligibleForPass() {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(KEY);
        return sortedSet.entryRange(0, PASS_SIZE)
                .stream().map(e -> e.getValue()).collect(Collectors.toList());
    }

    @Override
    public Integer removeTokensBeforeTimeStamp(Long timeStamp) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(KEY);
        return sortedSet.removeRangeByScore(0, true, timeStamp, true);
    }

    @Override
    public int countAll() {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(KEY);
        return sortedSet.size();
    }
}
