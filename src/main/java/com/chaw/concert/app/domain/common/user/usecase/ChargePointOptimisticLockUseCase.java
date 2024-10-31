package com.chaw.concert.app.domain.common.user.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class ChargePointOptimisticLockUseCase {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Output execute(Input input) {
        Point point = pointRepository.findByUserId(input.userId());
        try {
            point.increaseBalance(input.point());
            pointRepository.save(point);

            PointHistory pointHistory = PointHistory.createCharge(point.getId(), input.point());
            pointHistoryRepository.save(pointHistory);

            entityManager.flush();

            log.info("{} 포인트 충전", input.point());
            return new Output(point.getBalance());
        } catch (OptimisticLockException e) {
            log.error("낙관락 실패");
            throw new BaseException(ErrorType.CONFLICT, "낙관락 실패");
        }
    }

    public record Input(
        Long userId,
        Integer point
    ) {}

    public record Output(
        Integer balance
    ) {}
}
