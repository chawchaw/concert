package com.chaw.concert.app.domain.common.user.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.infrastructure.redis.helper.RedissonRLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class ChargePointRedissonLockUseCase {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @RedissonRLock(key = Point.REDIS_LOCK_KEY)
    public Output execute(Input input) {
        Point point = pointRepository.findByUserId(input.userId());
        point.increaseBalance(input.point());
        pointRepository.save(point);

        PointHistory pointHistory = PointHistory.createCharge(point.getId(), input.point());
        pointHistoryRepository.save(pointHistory);

        log.info("{} 포인트 충전", input.point());
        return new Output(point.getBalance());
    }

    public record Input(
        Long userId,
        Integer point
    ) {}

    public record Output(
        Integer balance
    ) {}
}
