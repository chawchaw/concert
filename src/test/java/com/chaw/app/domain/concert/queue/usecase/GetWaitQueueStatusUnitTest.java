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
        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(1L)
                .uuid("test-uuid")
                .build();

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(1L)
                .waitingUserId(10L)
                .build();

        List<WaitQueue> waitQueues = new ArrayList<>();
        waitQueues.add(WaitQueue.builder().id(11L).concertId(1L).uuid("test-uuid").build());

        when(queuePositionTrackerRepository.findByConcertId(1L)).thenReturn(queuePositionTracker);
        when(waitQueueRepository.existsByConcertIdAndUuid(1L, "test-uuid")).thenReturn(true);
        when(waitQueueRepository.findByConcertIdAndIdGreaterThanOrderByIdAsc(1L, 10L)).thenReturn(waitQueues);

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(1, result.getQueuePosition());
        assertFalse(result.getIsReservationPhase());

        verify(queuePositionTrackerRepository, times(1)).findByConcertId(1L);
        verify(waitQueueRepository, times(1)).existsByConcertIdAndUuid(1L, "test-uuid");
        verify(waitQueueRepository, times(1)).findByConcertIdAndIdGreaterThanOrderByIdAsc(1L, 10L);
    }

    @Test
    void shouldThrowUserNotInQueueExceptionWhenUserDoesNotExist() {
        // given
        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(1L)
                .uuid("test-uuid")
                .build();

        QueuePositionTracker queuePositionTracker = QueuePositionTracker.builder()
                .concertId(1L)
                .waitingUserId(10L)
                .build();

        List<WaitQueue> waitQueues = new ArrayList<>();

        when(queuePositionTrackerRepository.findByConcertId(1L)).thenReturn(queuePositionTracker);
        when(waitQueueRepository.existsByConcertIdAndUuid(1L, "test-uuid")).thenReturn(false);

        // when & then
        assertThrows(UserNotInQueueException.class, () -> {
            getWaitQueueStatus.execute(input);
        });

        verify(queuePositionTrackerRepository, times(1)).findByConcertId(1L);
        verify(waitQueueRepository, times(1)).existsByConcertIdAndUuid(1L, "test-uuid");
    }

    @Test
    void shouldReturnZeroWhenUserIsInReservationPhase() {
        // given
        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(1L)
                .uuid("test-uuid")
                .build();

        ReservationPhase reservationPhase = ReservationPhase.builder()
                .concertId(1L)
                .uuid("test-uuid")
                .build();

        when(reservationPhaseRepository.findByConcertIdAndUuid(1L, "test-uuid")).thenReturn(Optional.of(reservationPhase));

        // when
        GetWaitQueueStatus.Output result = getWaitQueueStatus.execute(input);

        // then
        assertNotNull(result);
        assertEquals(0, result.getQueuePosition());
        assertTrue(result.getIsReservationPhase());

        verify(reservationPhaseRepository, times(1)).findByConcertIdAndUuid(1L, "test-uuid");
        verify(queuePositionTrackerRepository, never()).findByConcertId(anyLong());
        verify(waitQueueRepository, never()).existsByConcertIdAndUuid(anyLong(), anyString());
    }

    @Test
    void shouldThrowWaitQueueIndicatorNotExistWhenNoQueueIndicatorFound() {
        // given
        GetWaitQueueStatus.Input input = GetWaitQueueStatus.Input.builder()
                .concertId(1L)
                .uuid("test-uuid")
                .build();

        when(reservationPhaseRepository.findByConcertIdAndUuid(1L, "test-uuid")).thenReturn(Optional.empty());
        when(queuePositionTrackerRepository.findByConcertId(1L)).thenReturn(null);

        // when & then
        assertThrows(WaitQueueIndicatorNotExist.class, () -> {
            getWaitQueueStatus.execute(input);
        });

        verify(queuePositionTrackerRepository, times(1)).findByConcertId(1L);
    }
}
