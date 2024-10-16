package com.chaw.app.domain.concert.queue.usecase;

import com.chaw.concert.app.domain.concert.queue.entity.QueuePositionTracker;
import com.chaw.concert.app.domain.concert.queue.entity.ReservationPhase;
import com.chaw.concert.app.domain.concert.queue.entity.WaitQueue;
import com.chaw.concert.app.domain.concert.queue.exception.WaitQueueIndicatorNotExist;
import com.chaw.concert.app.domain.concert.queue.exception.UserNotInQueueException;
import com.chaw.concert.app.domain.concert.queue.repository.QueuePositionTrackerRepository;
import com.chaw.concert.app.domain.concert.queue.repository.ReservationPhaseRepository;
import com.chaw.concert.app.domain.concert.queue.repository.WaitQueueRepository;
import com.chaw.concert.app.domain.concert.queue.usecase.GetWaitQueueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GetWaitQueueStatusUnitTest {

    @Mock
    private QueuePositionTrackerRepository queuePositionTrackerRepository;

    @Mock
    private WaitQueueRepository waitQueueRepository;

    @Mock
    private ReservationPhaseRepository reservationPhaseRepository;

    @InjectMocks
    private GetWaitQueueStatus getWaitQueueStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    void shouldReturnPositionInWaitQueue() {
        // given
        GetWaitQueueStatus.Input input = new GetWaitQueueStatus.Input(1L, "test-uuid");

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertScheduleId(1L)
                .waitQueueId(10L)
                .build();

        List<WaitQueue> waitQueues = new ArrayList<>();
        waitQueues.add(WaitQueue.builder().id(11L).concertScheduleId(1L).uuid("test-uuid").build());

        when(queuePositionTrackerRepository.findByConcertScheduleId(1L)).thenReturn(queuePositionTracker);
        when(waitQueueRepository.existsByConcertScheduleIdAndUuid(1L, "test-uuid")).thenReturn(true);
        when(waitQueueRepository.findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(1L, 10L)).thenReturn(waitQueues);

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(1, result.queuePosition());
        assertFalse(result.isReservationPhase());

        verify(queuePositionTrackerRepository, times(1)).findByConcertScheduleId(1L);
        verify(waitQueueRepository, times(1)).existsByConcertScheduleIdAndUuid(1L, "test-uuid");
        verify(waitQueueRepository, times(1)).findByConcertScheduleIdAndIdGreaterThanOrderByIdAsc(1L, 10L);
    }

    @Test
    void shouldThrowUserNotInQueueExceptionWhenUserDoesNotExist() {
        // given
        GetWaitQueueStatus.Input input = new GetWaitQueueStatus.Input(1L, "test-uuid");

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertScheduleId(1L)
                .waitQueueId(10L)
                .build();

        List<WaitQueue> waitQueues = new ArrayList<>();

        when(queuePositionTrackerRepository.findByConcertScheduleId(1L)).thenReturn(queuePositionTracker);
        when(waitQueueRepository.existsByConcertScheduleIdAndUuid(1L, "test-uuid")).thenReturn(false);

        // when & then
        assertThrows(UserNotInQueueException.class, () -> {
            getWaitQueueStatus.execute(input);
        });

        verify(queuePositionTrackerRepository, times(1)).findByConcertScheduleId(1L);
        verify(waitQueueRepository, times(1)).existsByConcertScheduleIdAndUuid(1L, "test-uuid");
    }

    @Test
    void shouldReturnZeroWhenUserIsInReservationPhase() {
        // given
        GetWaitQueueStatus.Input input = new GetWaitQueueStatus.Input(1L, "test-uuid");

        ReservationPhase reservationPhase = ReservationPhase.builder()
                .concertScheduleId(1L)
                .uuid("test-uuid")
                .build();

        when(reservationPhaseRepository.findByConcertScheduleIdAndUuid(1L, "test-uuid")).thenReturn(Optional.of(reservationPhase));

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(0, result.queuePosition());
        assertTrue(result.isReservationPhase());

        verify(reservationPhaseRepository, times(1)).findByConcertScheduleIdAndUuid(1L, "test-uuid");
        verify(queuePositionTrackerRepository, never()).findByConcertScheduleId(anyLong());
        verify(waitQueueRepository, never()).existsByConcertScheduleIdAndUuid(anyLong(), anyString());
    }

    @Test
    void shouldThrowWaitQueueIndicatorNotExistWhenNoQueueIndicatorFound() {
        // given
        GetWaitQueueStatus.Input input = new GetWaitQueueStatus.Input(1L, "test-uuid");

        when(reservationPhaseRepository.findByConcertScheduleIdAndUuid(1L, "test-uuid")).thenReturn(Optional.empty());
        when(queuePositionTrackerRepository.findByConcertScheduleId(1L)).thenReturn(null);

        // when & then
        assertThrows(WaitQueueIndicatorNotExist.class, () -> {
            getWaitQueueStatus.execute(input);
        });

        verify(queuePositionTrackerRepository, times(1)).findByConcertScheduleId(1L);
    }
}
