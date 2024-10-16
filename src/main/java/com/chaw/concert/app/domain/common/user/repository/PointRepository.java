package com.chaw.concert.app.domain.common.user.repository;

import com.chaw.concert.app.domain.common.user.entity.Point;

public interface PointRepository {

    Point findByUserId(Long userId);

    Point findByUserIdWithLock(Long userId);

    void save(Point point);

    Point findById(Long id);

    void deleteAll();

}
