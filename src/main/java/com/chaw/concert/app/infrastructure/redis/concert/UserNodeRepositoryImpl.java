package com.chaw.concert.app.infrastructure.redis.concert;

import com.chaw.concert.app.domain.concert.queue.entity.UserNode;
import com.chaw.concert.app.domain.concert.queue.entity.UserNodeStatus;
import com.chaw.concert.app.domain.concert.queue.repository.UserNodeRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Repository
public class UserNodeRepositoryImpl implements UserNodeRepository {

    private final RedissonClient redissonClient;
    private final UserNodeKeyHelper userNodeKeyHelper;

    private RScoredSortedSet<String> getWaitSortedSet() {
        return redissonClient.getScoredSortedSet(userNodeKeyHelper.getWaitKey());
    }

    @Override
    public UserNode findByUserId(Long userId) {
        UserNode userNodeActive = findUserNodeByUserIdAndActive(userId);

        if (userNodeActive != null) {
            return userNodeActive;
        }

        UserNode userNodeWait = findUserNodeByUserIdAndWait(userId);
        if (userNodeWait != null) {
            return userNodeWait;
        }

        return null;
    }

    @Override
    public List<UserNode> findTopWait(int size) {
        RScoredSortedSet<String> sortedSet = getWaitSortedSet();
        return sortedSet.entryRange(0, size - 1)
                .stream()
                .map(e -> UserNode.builder()
                        .userId(Long.parseLong(e.getValue()))
                        .score(e.getScore())
                        .status(UserNodeStatus.WAIT)
                        .build()) // 키와 점수를 record로 매핑
                .collect(Collectors.toList());
    }

    @Override
    public UserNode createWait(Long userId) {
        RScoredSortedSet<String> sortedSet = getWaitSortedSet();
        String key = userId.toString();
        long score = System.nanoTime();
        sortedSet.add(score, key);

        return findUserNodeByUserIdAndWait(userId);
    }

    @Override
    public UserNode createActive(Long userId) {
        String name = userNodeKeyHelper.getActiveKey(userId);
        redissonClient.getBucket(name).set(true);
        redissonClient.getBucket(name).expire(UserNode.TTL_ACTIVE_TOKEN_SECONDS, TimeUnit.SECONDS);

        return UserNode.builder()
                .userId(userId)
                .status(UserNodeStatus.ACTIVE)
                .build();
    }

    @Override
    public void activeUserNodes(List<UserNode> userNodes) {
        deleteUserNodes(userNodes);
        userNodes.forEach(userNode -> {
            createActive(userNode.getUserId());
        });
    }

    @Override
    public long countByStatus(UserNodeStatus status) {
        long count = 0;
        if (status == UserNodeStatus.WAIT) {
            RScoredSortedSet<String> sortedSet = getWaitSortedSet();
            count = sortedSet.size();
        }

        if (status == UserNodeStatus.ACTIVE) {
            RKeys keys = redissonClient.getKeys();
            count = StreamSupport.stream(keys.getKeysByPattern(userNodeKeyHelper.getActiveKeyPattern()).spliterator(), false).count();
        }

        return count;
    }

    private UserNode findUserNodeByUserIdAndActive(Long userId) {
        String activeKey = userNodeKeyHelper.getActiveKey(userId);
        boolean isActive = redissonClient.getBucket(activeKey).isExists();
        if (!isActive) {
            return null;
        }

        return UserNode.builder()
                .userId(userId)
                .status(UserNodeStatus.ACTIVE)
                .build();
    }

    private UserNode findUserNodeByUserIdAndWait(Long userId) {
        RScoredSortedSet<String> sortedSet = getWaitSortedSet();
        Integer rank = sortedSet.rank(userId.toString());
        if (rank == null) {
            return null;
        }

        return UserNode.builder()
                .userId(userId)
                .status(UserNodeStatus.WAIT)
                .rank(rank)
                .build();
    }

    private void deleteUserNodes(List<UserNode> userNodes) {
        if (userNodes.isEmpty()) {
            return;
        }
        Double score = userNodes.get(userNodes.size() - 1).getScore();
        RScoredSortedSet<String> sortedSet = getWaitSortedSet();
        sortedSet.removeRangeByScore(0, true, score, true);
    }

}
