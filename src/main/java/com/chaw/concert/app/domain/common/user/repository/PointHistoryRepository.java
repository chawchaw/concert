package com.chaw.concert.app.domain.common.user.repository;

import com.chaw.concert.app.domain.common.user.entity.PointHistory;

import java.util.List;

public interface PointHistoryRepository {
    void save(PointHistory pointHistory);

    List<PointHistory> findByPointId(Long userId);

    List<PointHistory> findAll();

    void deleteAll();

    PointHistory findById(Long id);
}
