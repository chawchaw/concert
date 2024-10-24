package com.chaw.concert.app.domain.common.user.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChargePoint {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public ChargePoint(PointRepository pointRepository, PointHistoryRepository pointHistoryRepository) {
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @Transactional
    public Output execute(Input input) {
        Point point = pointRepository.findByUserIdWithLock(input.userId());
        if (point == null) {
            point = Point.builder()
                    .userId(input.userId())
                    .balance(0)
                    .build();
        }

        point.setBalance(point.getBalance() + input.point());
        pointRepository.save(point);

        PointHistory pointHistory = PointHistory.builder()
                .pointId(point.getId())
                .type(PointHistoryType.CHARGE)
                .amount(input.point())
                .dateTransaction(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(pointHistory);

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
