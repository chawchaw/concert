package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.queue.entity.WaitToken;
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

    private final RedissonClient redissonClient;

    private RScoredSortedSet<String> getSortedSet() {
        return redissonClient.getScoredSortedSet(KEY);
    }

    private String getKeyByUserId(Long userId) {
        return userId.toString();
    }

    @Override
    public Integer getRankByUserId(Long userId) {
        RScoredSortedSet<String> sortedSet = getSortedSet();
        return sortedSet.rank(userId.toString());
    }

    @Override
    public boolean saveWithCurrentTime(Long userId) {
        RScoredSortedSet<String> sortedSet = getSortedSet();
        String key = getKeyByUserId(userId);
        long score = System.nanoTime();
        return sortedSet.add(score, key);
    }

    @Override
    public List<WaitToken> getTokensEligibleForPass(int endIndex) {
        RScoredSortedSet<String> sortedSet = getSortedSet();
        return sortedSet.entryRange(0, endIndex - 1)
                .stream()
                .map(e -> new WaitToken(e.getValue(), e.getScore())) // 키와 점수를 record로 매핑
                .collect(Collectors.toList());
    }

    @Override
    public Integer removeTokensBeforeTimeStamp(Double timeStamp) {
        RScoredSortedSet sortedSet = getSortedSet();
        return sortedSet.removeRangeByScore(0, true, timeStamp, true);
    }

    @Override
    public int countAll() {
        RScoredSortedSet sortedSet = getSortedSet();
        return sortedSet.size();
    }
}
