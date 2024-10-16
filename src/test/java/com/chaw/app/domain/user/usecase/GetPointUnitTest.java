package com.chaw.app.domain.user.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.GetPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class GetPointUnitTest {

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private GetPoint getPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mock 객체 초기화
    }

    @Test
    void testExistingPoint() {
        // Given: 기존 사용자 포인트가 있는 경우
        Point existingPoint = Point.builder()
                .userId(1L)
                .balance(100)
                .build();
        when(pointRepository.findById(anyLong())).thenReturn(existingPoint);

        // When: 요청 실행
        GetPoint.Input input = new GetPoint.Input(1L);
        GetPoint.Output output = getPoint.execute(input);

        // Then: 올바른 포인트 값이 반환되어야 함
        assertEquals(100, output.point());
    }

    @Test
    void testNewUserWithoutPoint() {
        // Given: 포인트가 없는 새로운 사용자
        when(pointRepository.findById(anyLong())).thenReturn(null);

        // When: 요청 실행
        GetPoint.Input input = new GetPoint.Input(1L);
        GetPoint.Output output = getPoint.execute(input);

        // Then: 새로운 포인트 객체가 생성되고, 잔액이 0이어야 함
        assertEquals(0, output.point());

        // 새로운 포인트가 저장되었는지 확인
        verify(pointRepository, times(1)).save(any(Point.class));
    }
}
