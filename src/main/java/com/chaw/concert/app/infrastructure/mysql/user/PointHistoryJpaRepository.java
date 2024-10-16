package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByPointId(Long pointId);
}
