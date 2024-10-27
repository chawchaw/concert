package com.chaw.app.domain.common.user.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.entity.PointHistory;
import com.chaw.concert.app.domain.common.user.repository.PointHistoryRepository;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import com.chaw.concert.app.domain.common.user.usecase.ChargePointUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChargePointUseCaseUnitTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private ChargePointUseCase chargePointUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mock 객체 초기화
    }

    @Test
    void testChargeExistingUser() {
        // Given: 이미 존재하는 사용자의 포인트
        Point existingPoint = Point.builder()
                .id(1L)
                .userId(1L)
                .balance(100)
                .build();
        when(pointRepository.findByUserIdWithLock(1L)).thenReturn(existingPoint);

        // When: 포인트 충전 요청
        ChargePointUseCase.Input input = new ChargePointUseCase.Input(1L, 50);
        ChargePointUseCase.Output output = chargePointUseCase.execute(input);

        // Then: 포인트가 정상적으로 충전되었는지 확인
        assertEquals(150, output.balance());
        verify(pointRepository, times(1)).save(any(Point.class));
        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
    }
}
