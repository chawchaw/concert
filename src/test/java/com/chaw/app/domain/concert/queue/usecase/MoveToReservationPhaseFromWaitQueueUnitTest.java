package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.MoveToReservationPhaseFromWaitQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class MoveToReservationPhaseFromWaitQueueTest {

    @Mock
    private ReservationPhaseRepository reservationPhaseRepository;

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @Mock
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    @InjectMocks
    private MoveToReservationPhaseFromWaitQueue moveToReservationPhaseFromWaitQueue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldMoveToReservationPhaseSuccessfully() {
        // given
        Long concertId = 999L;
        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(10L)
                .isWaitQueueExist(true)
                .build();

        List<WaitQueue> waitQueues = Arrays.asList(
                WaitQueue.builder().id(11L).concertId(concertId).userId(1L).uuid("uuid1").build(),
                WaitQueue.builder().id(12L).concertId(concertId).userId(2L).uuid("uuid2").build()
        );

        // Mock Repository 동작 설정
        when(queuePositionTrackerRepository.findAllByIsWaitQueueExist()).thenReturn(Collections.singletonList(queuePositionTracker));
        when(reservationPhaseRepository.countByConcertId(concertId)).thenReturn(0); // 예약 페이즈에 아직 아무도 없음
        when(waitQueueRepository.findByConcertIdAndIdGreaterThanOrderByIdAsc(concertId, 10L)).thenReturn(waitQueues);

        // when
        moveToReservationPhaseFromWaitQueue.execute();

        // then
        verify(reservationPhaseRepository, times(1)).saveAll(anyList());
        verify(queuePositionTrackerRepository, times(1)).save(queuePositionTracker);
        verify(waitQueueRepository, times(1)).findByConcertIdAndIdGreaterThanOrderByIdAsc(concertId, 10L);
    }

    @Test
    void shouldNotMoveWhenReservationPhaseIsFull() {
        // given
        Long concertId = 999L;
        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(10L)
                .isWaitQueueExist(true)
                .build();

        // Mock Repository 동작 설정
        when(queuePositionTrackerRepository.findAllByIsWaitQueueExist()).thenReturn(Collections.singletonList(queuePositionTracker));
        when(reservationPhaseRepository.countByConcertId(concertId)).thenReturn(30); // 예약 페이즈가 이미 꽉 찼음

        // when
        moveToReservationPhaseFromWaitQueue.execute();

        // then
        verify(reservationPhaseRepository, never()).saveAll(anyList());  // 예약 페이즈가 꽉 찼으므로 saveAll이 호출되지 않음
        verify(queuePositionTrackerRepository, never()).save(any());
        verify(waitQueueRepository, never()).findByConcertIdAndIdGreaterThanOrderByIdAsc(anyLong(), anyLong());
    }

    @Test
    void shouldMarkQueueAsNotExistingWhenEmpty() {
        // given
        Long concertId = 999L;
        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(concertId)
                .waitingUserId(10L)
                .isWaitQueueExist(true)
                .build();

        // 대기열에 아무도 없을 경우
        List<WaitQueue> emptyWaitQueue = Collections.emptyList();

        // Mock Repository 동작 설정
        when(queuePositionTrackerRepository.findAllByIsWaitQueueExist()).thenReturn(Collections.singletonList(queuePositionTracker));
        when(reservationPhaseRepository.countByConcertId(concertId)).thenReturn(0);
        when(waitQueueRepository.findByConcertIdAndIdGreaterThanOrderByIdAsc(concertId, 10L)).thenReturn(emptyWaitQueue);

        // when
        moveToReservationPhaseFromWaitQueue.execute();

        // then
        assertFalse(queuePositionTracker.getIsWaitQueueExist());  // 대기열이 없으므로 isWaitQueueExist 가 false 로 변경
        verify(queuePositionTrackerRepository, times(1)).save(queuePositionTracker);  // 상태 변경 후 저장
    }
}
