package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.EnterWaitQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnterWaitQueueUnitTest {

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @Mock
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    @InjectMocks
    private EnterWaitQueue enterWaitQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddUserToWaitQueueSuccessfully() {
        // given
        EnterWaitQueue.Input input = EnterWaitQueue.Input.builder()
                .userId(1L)
                .concertScheduleId(100L)
                .build();

        QueuePositionTracker mockIndicator = QueuePositionTracker.builder()
                .concertScheduleId(100L)
                .waitQueueId(0L)
                .isWaitQueueExist(true)
                .build();

        // Mock Indicator가 이미 존재하는 경우
        when(queuePositionTrackerRepository.findByConcertScheduleId(100L)).thenReturn(mockIndicator);

        // Mock 대기열 추가 성공
        WaitQueue mockWaitQueue = WaitQueue.builder()
                .userId(input.getUserId())
                .concertScheduleId(input.getConcertScheduleId())
                .uuid(UUID.randomUUID().toString())
                .build();
        when(waitQueueRepository.save(any(WaitQueue.class))).thenReturn(mockWaitQueue);

        // when
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // then
        WaitQueue waitQueue = output.getWaitQueue();
        assertNotNull(waitQueue);
        assertEquals(input.getUserId(), waitQueue.getUserId());
        assertEquals(input.getConcertScheduleId(), waitQueue.getConcertScheduleId());
        assertNotNull(waitQueue.getUuid());

        verify(queuePositionTrackerRepository, times(1)).findByConcertScheduleIdWithLock(input.getConcertScheduleId());
        verify(waitQueueRepository, times(1)).save(any(WaitQueue.class));
    }

    @Test
    void shouldCreateNewWaitQueueIndicatorIfNotExist() {
        // given
        EnterWaitQueue.Input input = EnterWaitQueue.Input.builder()
                .userId(1L)
                .concertScheduleId(100L)
                .build();

        // Mock Indicator가 없는 경우
        when(queuePositionTrackerRepository.findByConcertScheduleId(100L)).thenReturn(null);

        // Mock 대기열 추가 성공
        WaitQueue mockWaitQueue = WaitQueue.builder()
                .userId(input.getUserId())
                .concertScheduleId(input.getConcertScheduleId())
                .uuid(UUID.randomUUID().toString())
                .build();
        when(waitQueueRepository.save(any(WaitQueue.class))).thenReturn(mockWaitQueue);

        // when
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // then
        WaitQueue waitQueue = output.getWaitQueue();
        assertNotNull(waitQueue);
        assertEquals(input.getUserId(), waitQueue.getUserId());
        assertEquals(input.getConcertScheduleId(), waitQueue.getConcertScheduleId());
        assertNotNull(waitQueue.getUuid());

        verify(queuePositionTrackerRepository, times(1)).findByConcertScheduleIdWithLock(input.getConcertScheduleId());
        verify(queuePositionTrackerRepository, times(1)).save(any(QueuePositionTracker.class));
        verify(waitQueueRepository, times(1)).save(any(WaitQueue.class));
    }

    @Test
    void shouldActivateWaitQueueIfInactive() {
        // given
        EnterWaitQueue.Input input = EnterWaitQueue.Input.builder()
                .userId(1L)
                .concertScheduleId(100L)
                .build();

        // Mock Indicator가 존재하지만 대기열이 비활성화된 경우
        QueuePositionTracker inactiveIndicator = QueuePositionTracker.builder()
                .concertScheduleId(100L)
                .waitQueueId(0L)
                .isWaitQueueExist(false)  // 비활성화 상태
                .build();

        when(queuePositionTrackerRepository.findByConcertScheduleIdWithLock(100L)).thenReturn(Optional.of(inactiveIndicator));

        // Mock 대기열 추가 성공
        WaitQueue mockWaitQueue = WaitQueue.builder()
                .userId(input.getUserId())
                .concertScheduleId(input.getConcertScheduleId())
                .uuid(UUID.randomUUID().toString())
                .build();
        when(waitQueueRepository.save(any(WaitQueue.class))).thenReturn(mockWaitQueue);

        // when
        EnterWaitQueue.Output output = enterWaitQueue.execute(input);

        // then
        WaitQueue waitQueue = output.getWaitQueue();
        assertNotNull(waitQueue);
        assertEquals(input.getUserId(), waitQueue.getUserId());
        assertEquals(input.getConcertScheduleId(), waitQueue.getConcertScheduleId());
        assertNotNull(waitQueue.getUuid());

        verify(queuePositionTrackerRepository, times(1)).findByConcertScheduleIdWithLock(input.getConcertScheduleId());
        verify(queuePositionTrackerRepository, times(1)).save(any(QueuePositionTracker.class));  // 활성화 상태로 저장
        verify(waitQueueRepository, times(1)).save(any(WaitQueue.class));
    }

}
