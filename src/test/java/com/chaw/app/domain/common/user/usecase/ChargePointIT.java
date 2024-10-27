package com.chaw.app.domain.common.user.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.entity.PointHistoryType;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.ChargePoint;
import com.chaw.helper.DatabaseCleanupListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ConcertApplication.class)
@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        listeners = DatabaseCleanupListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class ChargePointIT {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private ChargePoint chargePoint;

    @Test
    void testChargeExistingUser() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(100)
                .build();
        pointRepository.save(point);

        // when
        ChargePoint.Input input = new ChargePoint.Input(1L, 50);
        ChargePoint.Output output = chargePoint.execute(input);

        // then
        assertEquals(150, output.balance());

        // verify
        PointHistory pointHistory = pointHistoryRepository.findAll().get(0);
        assertEquals(50, pointHistory.getAmount());
        assertEquals(PointHistoryType.CHARGE, pointHistory.getType());
    }

    @Test
    void testChargeNewUser() {
        // given

        // when
        ChargePoint.Input input = new ChargePoint.Input(2L, 100);
        ChargePoint.Output output = chargePoint.execute(input);

        // then
        Point newPoint = pointRepository.findByUserId(2L);
        assertEquals(100, newPoint.getBalance());

        // verify
        PointHistory pointHistory = pointHistoryRepository.findAll().get(0);
        assertEquals(100, pointHistory.getAmount());
        assertEquals(PointHistoryType.CHARGE, pointHistory.getType());
    }
}
