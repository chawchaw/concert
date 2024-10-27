package com.chaw.app.domain.common.user.usecase;

import com.chaw.concert.ConcertApplication;
import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.GetPoint;
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
public class GetPointIT {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private GetPoint getPoint;

    @Test
    void testExistingPoint() {
        // Given: 기존 사용자 포인트가 있을 때
        Point point = Point.builder()
                .userId(1L)
                .balance(100)
                .build();
        pointRepository.save(point);

        GetPoint.Input input = new GetPoint.Input(1L);

        // When: 요청 실행
        GetPoint.Output output = getPoint.execute(input);

        // Then: 포인트가 정상적으로 반환되어야 함
        assertEquals(100, output.point());
    }

    @Test
    void testNewUserWithoutPoint() {
        // Given: 포인트가 없는 새로운 사용자
        GetPoint.Input input = new GetPoint.Input(2L);

        // When: 요청 실행
        GetPoint.Output output = getPoint.execute(input);

        // Then: 새로운 사용자에게 포인트가 생성되고, 잔액이 0이어야 함
        assertEquals(0, output.point());

        // 새로운 포인트가 DB에 저장되었는지 확인
        Point newPoint = pointRepository.findByUserId(2L);
        assertEquals(0, newPoint.getBalance());
    }
}
