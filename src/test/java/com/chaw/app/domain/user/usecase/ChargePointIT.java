package com.chaw.app.domain.user.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.ChargePoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@Transactional
public class ChargePointIT {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private ChargePoint chargePoint;

    @AfterEach
    void tearDown() {
        pointRepository.deleteAll();
        pointHistoryRepository.deleteAll();
    }

    @Test
    void testChargeExistingUser() {
        // Given: 이미 존재하는 사용자의 포인트 설정
        Point point = Point.builder()
                .userId(1L)
                .balance(100)
                .build();
        pointRepository.save(point);

        // When: 포인트 충전 요청
        ChargePoint.Input input = new ChargePoint.Input(1L, 50);
        ChargePoint.Output output = chargePoint.execute(input);

        // Then: 포인트가 정상적으로 충전되었는지 확인
        assertEquals(150, output.balance());

        // 포인트 히스토리 저장 확인
        PointHistory pointHistory = pointHistoryRepository.findAll().get(0);
        assertEquals(50, pointHistory.getAmount());
        assertEquals(PointHistoryType.CHARGE, pointHistory.getType());
    }

    @Test
    void testChargeNewUser() {
        // Given: 새로운 사용자의 포인트 (존재하지 않음)

        // When: 포인트 충전 요청
        ChargePoint.Input input = new ChargePoint.Input(2L, 100);
        ChargePoint.Output output = chargePoint.execute(input);

        // Then: 새로운 사용자의 포인트가 생성되고 충전되었는지 확인
        Point newPoint = pointRepository.findByUserId(2L);
        assertEquals(100, newPoint.getBalance());

        // 포인트 히스토리 저장 확인
        PointHistory pointHistory = pointHistoryRepository.findAll().get(0);
        assertEquals(100, pointHistory.getAmount());
        assertEquals(PointHistoryType.CHARGE, pointHistory.getType());
    }
}
