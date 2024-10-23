package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.infrastructure.exception.BaseException;
import com.chaw.concert.app.infrastructure.exception.ErrorType;
import org.springframework.stereotype.Repository;

@Repository
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository repository;

    public PointRepositoryImpl(PointJpaRepository repository) {
        this.repository = repository;
    }

    private Point makePoint(Long userId) {
        Point point = Point.builder()
                .userId(userId)
                .balance(0)
                .build();
        repository.save(point);
        return point;
    }

    @Override
    public Point findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new BaseException(ErrorType.NOT_FOUND, "Point not found"));
    }

    @Override
    public Point findByUserId(Long userId) {
        Point point = repository.findByUserId(userId);
        if (point == null) {
            point = makePoint(userId);
        }
        return point;
    }

    @Override
    public Point findByUserIdWithLock(Long userId) {
        Point point = repository.findByUserIdWithLock(userId);
        if (point == null) {
            point = makePoint(userId);
        }
        return point;
    }

    @Override
    public void save(Point point) {
        repository.save(point);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
