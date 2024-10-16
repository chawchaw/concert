package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository repository;

    public PointRepositoryImpl(PointJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Point findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Point findByUserIdWithLock(Long userId) {
        return repository.findByUserIdWithLock(userId);
    }

    @Override
    public void save(Point point) {
        repository.save(point);
    }

    @Override
    public Point findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
