package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository repository;

    public PointHistoryRepositoryImpl(PointHistoryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(PointHistory pointHistory) {
        repository.save(pointHistory);
    }

    @Override
    public List<PointHistory> findByPointId(Long pointId) {
        return repository.findByPointId(pointId);
    }

    @Override
    public List<PointHistory> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
